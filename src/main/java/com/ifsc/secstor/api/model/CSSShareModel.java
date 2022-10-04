package com.ifsc.secstor.api.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CSSShareModel {
    private List<IndexKeyPair> shares;
    private List<IndexKeyPair> fingerprints;
    private List<IndexKeyPair> encKeys;
    private Integer originalLength;
    private Integer encAlgorithm;

    @Override
    public String toString() {
        return "CSSShareModel";
    }
}
