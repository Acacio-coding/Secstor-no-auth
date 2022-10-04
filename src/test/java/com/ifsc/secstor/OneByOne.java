package com.ifsc.secstor;

import com.at.archistar.crypto.data.CSSShare;
import com.at.archistar.crypto.data.KrawczykShare;
import com.at.archistar.crypto.data.PSSShare;
import com.at.archistar.crypto.data.Share;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.ifsc.secstor.utils.*;
import com.ifsc.secstor.utils.facade.EngineMaker;
import com.ifsc.secstor.utils.models.*;
import com.ifsc.secstor.utils.pvss.Shares;


import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class OneByOne {
    public static void main(String[] args) {

        final int N = Integer.parseInt(args[0]);
        final int K = Integer.parseInt(args[1]);
        final List<Integer> numberOfKeysToReconstruct = new ArrayList<>();

        for (int i = 2; i < (args.length - 2); i++) {
            numberOfKeysToReconstruct.add(Integer.valueOf(args[i]));
        }

        final String DATASET_FILE_NAME = args[args.length - 2];
        final int threadNumber = Integer.parseInt(args[args.length - 1]);

        final List<JsonObject> splitData = InputReader.getInputToJson(DATASET_FILE_NAME);
        final int datasetSize = splitData.get(0).toString().getBytes(StandardCharsets.UTF_8).length / 1024;
        final int objectCount = splitData.size();

        double start = System.currentTimeMillis();

        List<SplitTimingResult> splitResults = getSplitTimings(N, K, splitData, threadNumber);

        writeSplitResults(splitResults, N, K, datasetSize, threadNumber, objectCount);

        writeJsonResults(splitResults, N, K, datasetSize, objectCount);

        List<Object> reconstructData = mergeModels(splitResults);

        List<ReconstructTimingResult> reconstructResults =
                getReconstructTimings(N, K, reconstructData, numberOfKeysToReconstruct, threadNumber);

        writeReconstructResults(reconstructResults, N, K, datasetSize, threadNumber,
                numberOfKeysToReconstruct, objectCount);

        double end = (System.currentTimeMillis() - start);

        if (end > 60000) {
            long result = TimeUnit.MILLISECONDS.toMinutes((long) end);

            if (result > 1)
                System.out.println("Total test elapsed time: " + result + " minutes\n");
            else
                System.out.println("Total test elapsed time: " + result + " minute\n");
        } else if (end > 1000) {
            System.out.println("Total test elapsed time: " + TimeUnit.MILLISECONDS.toSeconds((long) end) + " seconds\n");
        } else {
            System.out.println("Total test elapsed time: " + end + " milliseconds\n");
        }
    }

    private static void sortSplitResults(List<SplitTimingResult> splitResults) {
        splitResults.sort((result1, result2) ->  {
            int number1 = result1.threadId().charAt(result1.threadId().length() - 1);
            int number2 = result2.threadId().charAt(result2.threadId().length() - 1);

            return Math.min(number1, number2);
        });

    }

    private static void sortReconstructResults(List<ReconstructTimingResult> reconstructResults) {
        reconstructResults.sort((result1, result2) ->  {
            int number1 = result1.threadId().charAt(result1.threadId().length() - 1);
            int number2 = result2.threadId().charAt(result2.threadId().length() - 1);

            return Math.min(number1, number2);
        });
    }
    private static List<ReconstructTimingResult> getReconstructTimings(int N, int K, List<Object> data,
                                                                       List<Integer> numberOfKeys, int threadNumber) {
        System.out.println("Started reconstruct...");
        for (Integer number : numberOfKeys) {
            if (number > N || number < K) {
                throw new RuntimeException("Each number of keys must be < N and >= K");
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        List<ReconstructTimingResult> reconstructResults = new ArrayList<>();

        try {
            for (int i = 0; i < threadNumber; i++) {
                Future<ReconstructTimingResult> future = executor
                        .submit(new ReconstructTimingGetter(new EngineMaker(N, K), data, 5, numberOfKeys));
                reconstructResults.add(future.get());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }

        return reconstructResults;
    }

    private static List<Object> mergeModels(List<SplitTimingResult> rawModels) {
        List<Object> mergedModels = new ArrayList<>();

        for (SplitTimingResult singleResult : rawModels) {
            for (int j = 0; j < singleResult.results().size(); j++) {
                MapAlgorithmModel current = singleResult.results().get(j);
                mergedModels.addAll(current.models());
            }
        }

        return mergedModels;
    }

    private static List<SplitTimingResult> getSplitTimings(int N, int K, List<JsonObject> data, int threadNumber) {
        System.out.println("\nStarted split...");
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        List<SplitTimingResult> splitResults = new ArrayList<>();

        try {
            for (int i = 0; i < threadNumber; i++) {
                Future<SplitTimingResult> future = executor
                        .submit(new SplitTimingGetter(new EngineMaker(N, K), data, 5));
                splitResults.add(future.get());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }

        return splitResults;
    }

    private static void writeSplitResults(List<SplitTimingResult> results, int N, int K, int dataSize,
                                          int threadCount, int objectCount) {
        String path = "./timing-tests/results/split/";

        //Ordena os resultados pelo número da Thread
        sortSplitResults(results);

        for (SplitTimingResult singleResult : results) {
            for (int j = 0; j < singleResult.results().size(); j++) {
                MapAlgorithmModel current = singleResult.results().get(j);
                String algorithm = current.algorithm().toLowerCase(Locale.ROOT);
                Multimap<Integer, String> timings = current.timings();

                String currentFileName = OutputWriter.
                        createFileName(algorithm,
                                N + "-" + K,
                                dataSize,
                                threadCount,
                                "split",
                                objectCount);

                OutputWriter.writeSplitResults(singleResult.threadId(), timings, path, currentFileName);
            }
        }
    }

    private static void writeReconstructResults(List<ReconstructTimingResult> results, int N, int K, int dataSize,
                                                int threadCount, List<Integer> numbers, int objectCount) {

        String path = "./timing-tests/results/reconstruct/";

        //Ordena os resultados pelo número da Thread
        sortReconstructResults(results);

        List<String> keyNumbers = new ArrayList<>(numbers.stream().map(String::valueOf).toList());

        for (ReconstructTimingResult singleResultList : results) {
            for (int i = 0; i < singleResultList.results().size(); i++) {
                List<MapAlgorithmKeyNumber> listOfResults = singleResultList.results().get(i);

                String algorithm = listOfResults.get(0).algorithm().toLowerCase(Locale.ROOT);

                List<Multimap<Integer, String>> timings =
                        new ArrayList<>(listOfResults.stream().map(MapAlgorithmKeyNumber::timings).toList());

                String currentFileName = OutputWriter.
                        createFileName(algorithm,
                                N + "-" + K,
                                dataSize,
                                threadCount,
                                "reconstruct",
                                objectCount);

                OutputWriter.writeReconstructResults(singleResultList.threadId(),
                        timings, path, currentFileName, keyNumbers);
            }
        }
    }

    private static void writeJsonResults(List<SplitTimingResult> results, int N, int K, int dataSize, int objectCount) {
        String jsonFileName = OutputWriter.createFileName(N + "-" + K, dataSize, objectCount);
        Path path = Path.of("./timing-tests/datasets/reconstruct/" + jsonFileName);

        if (path.toFile().exists() || path.toFile().isFile())
            return;

        List<Object> mergedModels = new ArrayList<>();

        for (SplitTimingResult singleResult : results) {
            for (int j = 0; j < singleResult.results().size(); j++) {
                MapAlgorithmModel current = singleResult.results().get(j);
                mergedModels.addAll(castModels(current.models()));
            }
        }

        OutputWriter.writeJsonResults(mergedModels, path);
    }

    private static List<Object> castModels(List<Object> rawModels) {
        List<Object> toReturn = new ArrayList<>();

        for (Object model : rawModels) {
            Object toAdd = null;

            if (model instanceof Share[]) {
                for (int j = 0; j < ((Share[]) model).length; j++) {
                    if (((Share[]) model)[j].getShareType().equalsIgnoreCase("shamir")) {
                        toAdd = new ShamirShareModel(new ArrayList<>(), ((Share[]) model)[j].getOriginalLength());

                        fillShamirShareModel(((Share[]) model), (ShamirShareModel) toAdd);
                    } else if (((Share[]) model)[j].getShareType().equalsIgnoreCase("pss")) {
                        toAdd = new PSSShareModel(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                                ((Share[]) model)[j].getOriginalLength());

                        fillPSSShareModel(((Share[]) model), (PSSShareModel) toAdd);
                    } else if (((Share[]) model)[j].getShareType().equalsIgnoreCase("css")) {
                        toAdd = new CSSShareModel(new ArrayList<>(), new ArrayList<>(),
                                new ArrayList<>(), ((Share[]) model)[j].getOriginalLength(),
                                ((CSSShare) ((Share[]) model)[j]).getEncAlgorithm());

                        fillCSSShareModel(((Share[]) model), (CSSShareModel) toAdd);
                    } else {
                        toAdd = new KrawczykShareModel(new ArrayList<>(), new ArrayList<>(),
                                ((Share[]) model)[j].getOriginalLength(),
                                ((KrawczykShare) ((Share[]) model)[j]).getEncAlgorithm());

                        fillKrawczykShareModel(((Share[]) model), (KrawczykShareModel) toAdd);
                    }
                }
            } else {
                toAdd = new PVSSShareModel(
                        ((Shares) model).getShares(),
                        ((Shares) model).getShares().get(0).key().length(),
                        ((Shares) model).getKey(),
                        ((Shares) model).getKey().length(),
                        ((Shares) model).getModulus(),
                        ((Shares) model).getModulus().toString().length());
            }

            toReturn.add(toAdd);
        }

        return toReturn;
    }

    private static void fillShamirShareModel(Share[] generatedShares, ShamirShareModel toReturn) {
        for (Share share : generatedShares) {
            toReturn.getShares()
                    .add(new IndexKeyPair(share.getX(), Base64.getEncoder().encodeToString(share.getYValues())));
        }
    }

    private static void fillPSSShareModel(Share[] generatedShares, PSSShareModel toReturn) {
        for (Share share : generatedShares) {
            toReturn.getShares()
                    .add(new IndexKeyPair(share.getX(), Base64.getEncoder().encodeToString(share.getYValues())));


            List<IndexKeyPair> auxMKeys = new ArrayList<>();
            List<IndexKeyPair> auxMacs = new ArrayList<>();

            int j = 1;
            for (int i = 0; i < generatedShares.length; i++) {
                auxMKeys.add(new IndexKeyPair(j,
                        Base64.getEncoder().encodeToString(((PSSShare) share).getMacKeys().get((byte) j))));

                auxMacs.add(new IndexKeyPair(j,
                        Base64.getEncoder().encodeToString(((PSSShare) share).getMacs().get((byte) j))));
                j++;
            }

            toReturn.getMacKeys().add(new IndexArrayPair(share.getX(), auxMKeys));
            toReturn.getMacs().add(new IndexArrayPair(share.getX(), auxMacs));
        }
    }

    private static void fillCSSShareModel(Share[] generatedShares, CSSShareModel toReturn) {
        int i = 1;
        for (Share share : generatedShares) {
            toReturn.getShares()
                    .add(new IndexKeyPair(share.getX(), Base64.getEncoder().encodeToString(share.getYValues())));

            toReturn.getFingerprints().
                    add(new IndexKeyPair(i, Base64.getEncoder().encodeToString(((CSSShare) share).getFingerprints()
                            .get((byte) i))));

            toReturn.getEncKeys()
                    .add(new IndexKeyPair(i, Base64.getEncoder().encodeToString(((CSSShare) share).getKey())));

            i++;
        }
    }

    private static void fillKrawczykShareModel(Share[] generatedShares, KrawczykShareModel toReturn) {
        for (Share share : generatedShares) {
            toReturn.getShares().add(new IndexKeyPair(share.getX(),
                    Base64.getEncoder().encodeToString(share.getYValues())));

            toReturn.getEncKeys().add(new IndexKeyPair(share.getX(),
                    Base64.getEncoder().encodeToString(((KrawczykShare) share).getKey())));
        }
    }
}
