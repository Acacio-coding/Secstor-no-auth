package com.ifsc.secstor.api.model;

import lombok.Getter;

@Getter
public class ValidationErrorModel extends ErrorModel {
    public ValidationErrorModel(int status, String title, String message, String path) {
        super(status, title, message, path);
    }
}

