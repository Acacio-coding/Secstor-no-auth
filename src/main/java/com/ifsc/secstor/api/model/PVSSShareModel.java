package com.ifsc.secstor.api.model;

import lombok.*;

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
