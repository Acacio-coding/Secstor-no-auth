package com.ifsc.secstor.utils;

import com.google.common.collect.Multimap;

public record MapAlgorithmKeyNumber(String algorithm, Multimap<Integer, String> timings, String keyNumber) {
}
