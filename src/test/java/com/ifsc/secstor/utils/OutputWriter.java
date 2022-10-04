package com.ifsc.secstor.utils;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OutputWriter {

    public static void writeSplitResults(String threadId, Multimap<Integer, String> results,
                                         String path, String filename) {
        Path pathObject = Path.of(path + filename);
        List<String> lines = new ArrayList<>();

        try {
            if (threadId.equals("Thread-1")) {
                String headLine = "thread;registro;tempo1;tempo2;tempo3;tempo4;tempo5";
                lines.add(headLine);
            }

            List<Integer> registers = results.keySet().stream().sorted().toList();
            List<Collection<String>> timings = registers.stream().map(results::get).toList();

            for (int i = 0; i < registers.size(); i++) {
                Integer register = registers.get(i);
                StringBuilder line = new StringBuilder(threadId + ';' + register + ';');
                List<String> currentTimings = (List<String>) timings.get(i);

                for (int k = 0; k < currentTimings.size(); k++) {
                    String elapsedTime;

                    if (k < currentTimings.size() - 1) {
                        elapsedTime = currentTimings.get(k) + ';';
                    } else {
                        elapsedTime = currentTimings.get(k);
                    }

                    line.append(elapsedTime);
                }

                lines.add(String.valueOf(line));
            }

            Files.write(pathObject, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void writeReconstructResults(String threadId, List<Multimap<Integer, String>> results,
                                               String path, String filename, List<String> keyNumbers) {
        Path pathObject = Path.of(path + filename);
        List<String> lines = new ArrayList<>();

        try {
            if (threadId.equals("Thread-1")) {
                String headLine = "thread;registro;número de chaves;tempo1;tempo2;tempo3;tempo4;tempo5";
                lines.add(headLine);
            }

            for (int i = 0; i < results.size(); i++) {
                Multimap<Integer, String> result = results.get(i);
                List<Integer> registers = result.keySet().stream().sorted().toList();
                List<Collection<String>> timings = registers.stream().map(result::get).toList();
                String keyNumber = keyNumbers.get(i);

                for (Integer register : registers) {
                    StringBuilder line = new StringBuilder(threadId + ';' + register + ';' + keyNumber + ';');

                    List<String> currentTimings = (List<String>) timings.get(i);

                    for (int k = 0; k < currentTimings.size(); k++) {
                        String elapsedTime;

                        if (k < currentTimings.size() - 1) {
                            elapsedTime = currentTimings.get(k) + ';';
                        } else {
                            elapsedTime = currentTimings.get(k);
                        }

                        line.append(elapsedTime);
                    }

                    lines.add(String.valueOf(line));
                }
            }

            Files.write(pathObject, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeJsonResults(List<Object> models, Path filename) {
        try(BufferedWriter writer = Files.newBufferedWriter(filename)) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            gson.toJson(models, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String createFileName(String params, int dataSize, int objectCount) {
        return dataSize + "kB_" + objectCount + "objects_dataset_" + params + "_to_reconstruct.json";
    }

    public static String createFileName(String algorithm, String params ,int dataSize, int threadCount,
                                        String operation, int objectCount) {
        return operation + "_" +
               algorithm + "_" +
               params + "_" +
               dataSize + "kB_" +
               threadCount + (threadCount > 1 ? "threads_" : "thread_") +
               objectCount + "objects.csv";
    }
}
