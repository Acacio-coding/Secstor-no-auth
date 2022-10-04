package com.ifsc.secstor.utils.pvss;

import com.ifsc.secstor.utils.IndexKeyPair;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@AllArgsConstructor
@Getter
public class Shares {

    private final List<IndexKeyPair> shares;
    private final BigInteger modulus;
    private final String Key;

    @Override
    public String toString() {
        return "PVSS";
    }
}
