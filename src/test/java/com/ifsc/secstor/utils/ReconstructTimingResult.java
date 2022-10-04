package com.ifsc.secstor.utils;

import java.util.List;

public record ReconstructTimingResult(String threadId, List<List<MapAlgorithmKeyNumber>> results) {
}
