package com.ifsc.secstor.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

public class InputReader {
    public static List<JsonObject> getInputToJson(String fileName) {
        String path = "./timing-tests/datasets/split/" + fileName;

        try (JsonReader input = new JsonReader(new FileReader(path))) {
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<List<JsonObject>>(){}.getType();
            return gson.fromJson(input, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
