package com.ifsc.secstor.api.model;

import lombok.Getter;

@Getter
public class ReconstructErrorModel extends ErrorModel {

    private final int keyIndex;
    private final String key;
    private final String type;

    public ReconstructErrorModel(int status, String title, String message, String path, int keyIndex, String key, String type) {
        super(status, title, message, path);
        this.keyIndex = keyIndex;
        this.key = key;
        this.type = type;
    }
}
