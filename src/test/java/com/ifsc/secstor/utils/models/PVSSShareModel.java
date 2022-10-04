package com.ifsc.secstor.utils.models;

import com.ifsc.secstor.utils.IndexKeyPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PVSSShareModel {
    private List<IndexKeyPair> shares;
    private int shareOriginalLength;
    private String key;
    private int keyOriginalLength;
    private BigInteger modulus;
    private int modulusOriginalLength;
}
