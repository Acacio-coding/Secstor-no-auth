package com.ifsc.secstor.api.advice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class ReconstructException extends RuntimeException {
    private final HttpStatus status;
    private final String title = "Reconstruction Error";
    private final String message;
    private final int keyIndex;
    private final String key;
    private final String type;
    private final String path;
}
