package com.ifsc.secstor.utils;

import com.at.archistar.crypto.data.Share;
import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ifsc.secstor.utils.facade.Engine;
import com.ifsc.secstor.utils.facade.EngineMaker;
import com.ifsc.secstor.utils.pvss.Shares;
import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;


import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public record ReconstructTimingGetter(EngineMaker engineMaker, List<Object> data, int turns, List<Integer> numberOfKeys)
        implements Callable<ReconstructTimingResult> {

    private MapAlgorithmKeyNumber getReconstructTiming(Engine engine, List<Object> data, int keyNumber)
            throws UnsupportedEncodingException,
            InvalidVSSScheme, ReconstructionException, com.ifsc.secstor.utils.pvss.InvalidVSSScheme {
        String algorithm = engine.getAlgorithm();
        Multimap<Integer, String> timings = ArrayListMultimap.create();

        NumberFormat formatter = new DecimalFormat("#0.000",
                DecimalFormatSymbols.getInstance(Locale.US));

        for (int i = 0; i < data.size(); i++) {
            Object register = data.get(i);
            engine.setShares(register);
            for (int j = 0; j < turns; j++) {
                double start = System.nanoTime();
                engine.reconstruct();
                double end = (System.nanoTime() - start) / 1e+6;

                timings.put((i + 1), formatter.format(end));
            }
        }

        return new MapAlgorithmKeyNumber(algorithm, timings, String.valueOf(keyNumber));
    }

    private List<Object> getFilteredObjects(String algorithm, int numberOfKeys) {
        List<Object> filtered;

        if (algorithm.equalsIgnoreCase("Shamir") || algorithm.equalsIgnoreCase("PSS") ||
                algorithm.equalsIgnoreCase("CSS") || algorithm.equalsIgnoreCase("Krawczyk")) {
            filtered = data.stream()
                    .filter(current -> current instanceof Share[] && Arrays.stream(((Share[]) current))
                            .allMatch(it -> it.getShareType().equalsIgnoreCase(algorithm)))
                    .toList();

            filtered = filtered.stream()
                    .map(shareArray -> (Share[]) shareArray)
                    .map(shareArray -> {
                        Share[] toSubstitute = new Share[numberOfKeys];
                        System.arraycopy(shareArray, 0, toSubstitute, 0, numberOfKeys);
                        return toSubstitute;
                    })
                    .collect(Collectors.toList());
        } else {
            filtered = data.stream().filter(current -> current instanceof Shares && current.toString().
                    equalsIgnoreCase(algorithm)).toList();

            for (Object shareObject : filtered) {
                ((Shares) shareObject).getShares()
                        .removeIf(currentShare -> currentShare.index() > numberOfKeys);
            }
        }

        return filtered;
    }

    @Override
    public ReconstructTimingResult call() throws Exception {
        List<MapAlgorithmKeyNumber> shamirResults = new ArrayList<>();
        List<MapAlgorithmKeyNumber> pssResults = new ArrayList<>();
        List<MapAlgorithmKeyNumber> cssResults = new ArrayList<>();
        List<MapAlgorithmKeyNumber> krawczykResults = new ArrayList<>();
        List<MapAlgorithmKeyNumber> pvssResults = new ArrayList<>();

        for (Integer currentNumber : numberOfKeys) {
            List<Object> shamir = getFilteredObjects("Shamir", currentNumber);
            shamirResults.add(getReconstructTiming(engineMaker.getShamir(), shamir, currentNumber));

            List<Object> pss = getFilteredObjects("PSS", currentNumber);
            pssResults.add(getReconstructTiming(engineMaker.getPss(), pss, currentNumber));

            List<Object> css = getFilteredObjects("CSS", currentNumber);
            cssResults.add(getReconstructTiming(engineMaker.getCss(), css, currentNumber));

            List<Object> krawczyk = getFilteredObjects("Krawczyk", currentNumber);
            krawczykResults.add(getReconstructTiming(engineMaker.getKrawczyk(), krawczyk, currentNumber));

            List<Object> pvss = getFilteredObjects("PVSS", currentNumber);
            pvssResults.add(getReconstructTiming(engineMaker.getPvss(), pvss, currentNumber));
        }

        List<List<MapAlgorithmKeyNumber>> allResults = new ArrayList<>();
        allResults.add(shamirResults);
        allResults.add(pssResults);
        allResults.add(cssResults);
        allResults.add(krawczykResults);
        allResults.add(pvssResults);

        String thread = Thread.currentThread().getName();
        thread = thread.substring(Thread.currentThread().getName().indexOf("t"));
        thread = thread.substring(0, 1).toUpperCase() + thread.substring(1);

        return new ReconstructTimingResult(thread, allResults);
    }
}
