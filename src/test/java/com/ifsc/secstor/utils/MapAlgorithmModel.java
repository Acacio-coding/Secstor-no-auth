package com.ifsc.secstor.utils;

import com.google.common.collect.Multimap;

import java.util.List;

public record MapAlgorithmModel(String algorithm, Multimap<Integer, String> timings, List<Object> models) {
}
