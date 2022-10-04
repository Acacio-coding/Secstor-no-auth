package com.ifsc.secstor.utils.facade;


import com.ifsc.secstor.utils.IndexKeyPair;
import com.ifsc.secstor.utils.pvss.InvalidVSSScheme;
import com.ifsc.secstor.utils.pvss.SecretShareEngine;
import com.ifsc.secstor.utils.pvss.Shares;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class PVSSEngine implements Engine {
    private final SecretShareEngine engine;
    private Shares shares;

    public PVSSEngine(SecretShareEngine engine) {
        this.engine = engine;
    }

    public String getEngine() {
        return this.engine.toString();
    }

    @Override
    public void split(String data) throws InvalidVSSScheme {
        this.shares = engine.split(data);
    }

    @Override
    public String reconstruct() throws InvalidVSSScheme {
        return this.engine.combine(this.shares);
    }

    @Override
    public ArrayList<IndexKeyPair> getPieces() {
        return new ArrayList<>(this.shares.getShares());
    }

    @Override
    public String getAlgorithm() {
        return engine.toString();
    }

    @Override
    public void setShares(Object shares) {
        this.shares = (Shares) shares;
    }

}
