package com.ifsc.secstor.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ShamirShareModel {

    private List<IndexKeyPair> shares;
    private int originalLength;
    @Override
    public String toString() {
        return "ShamirShareModel";
    }
}
