package com.ifsc.secstor.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.ifsc.secstor.utils.facade.ArchistarEngine;
import com.ifsc.secstor.utils.facade.Engine;
import com.ifsc.secstor.utils.facade.EngineMaker;
import com.ifsc.secstor.utils.facade.PVSSEngine;
import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

public record SplitTimingGetter(EngineMaker engineMaker, List<JsonObject> data, int turns)
        implements Callable<SplitTimingResult> {
    private MapAlgorithmModel getSplitTimings(Engine engine, String thread) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidVSSScheme, com.ifsc.secstor.utils.pvss.InvalidVSSScheme {

        String algorithm = engine.getAlgorithm();
        Multimap<Integer, String> timings = ArrayListMultimap.create();
        List<Object> models = new ArrayList<>();

        NumberFormat formatter = new DecimalFormat("#0.000",
                DecimalFormatSymbols.getInstance(Locale.US));

        for (int i = 0; i < data.size(); i++) {
            JsonObject register = data.get(i);

            for (int j = 0; j < turns; j++) {
                double start = System.nanoTime();
                engine.split(register.toString());
                double end = (System.nanoTime() - start) / 1e+6;

                if (engine instanceof ArchistarEngine && j == 0 && thread.equals("Thread-1"))
                    models.add(((ArchistarEngine) engine).getShares());

                if (engine instanceof PVSSEngine && j == 0 && thread.equals("Thread-1"))
                    models.add(((PVSSEngine) engine).getShares());

                timings.put((i + 1), formatter.format(end));
            }
        }

        return new MapAlgorithmModel(algorithm, timings, models);
    }

    @Override
    public SplitTimingResult call() throws Exception {
        String thread = Thread.currentThread().getName();
        thread = thread.substring(thread.indexOf("t"));
        thread = thread.substring(0, 1).toUpperCase() + thread.substring(1);

        List<MapAlgorithmModel> results = new ArrayList<>();

        results.add(getSplitTimings(engineMaker.getShamir(), thread));
        results.add(getSplitTimings(engineMaker.getPss(), thread));
        results.add(getSplitTimings(engineMaker.getCss(), thread));
        results.add(getSplitTimings(engineMaker.getKrawczyk(), thread));
        results.add(getSplitTimings(engineMaker.getPvss(), thread));

        return new SplitTimingResult(thread, results);
    }
}
