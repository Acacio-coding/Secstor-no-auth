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
public class ShamirShareModel {

    private List<IndexKeyPair> shares;
    private int originalLength;
    @Override
    public String toString() {
        return "ShamirShareModel";
    }
}
