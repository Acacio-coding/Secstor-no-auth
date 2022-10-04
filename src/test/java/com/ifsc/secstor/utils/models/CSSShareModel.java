package com.ifsc.secstor.utils.models;

import com.ifsc.secstor.utils.IndexKeyPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
