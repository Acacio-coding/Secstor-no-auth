package com.ifsc.secstor.utils.facade;

import com.at.archistar.crypto.CryptoEngine;
import com.at.archistar.crypto.data.ReconstructionResult;
import com.at.archistar.crypto.data.Share;
import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.ifsc.secstor.utils.IndexKeyPair;
import com.ifsc.secstor.utils.facade.Engine;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

@Getter
public class ArchistarEngine implements Engine {
    private final CryptoEngine engine;
    private Share[] shares;

    public ArchistarEngine(CryptoEngine engine) {
        this.engine = engine;
    }

    public String getEngine() {
        return this.engine.toString();
    }

    @Override
    public void split(String data) {
        this.shares = engine.share(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String reconstruct() throws ReconstructionException {
        ReconstructionResult result = engine.reconstruct(this.shares);
        return new String(result.getData(), StandardCharsets.UTF_8);
    }

    @Override
    public ArrayList<IndexKeyPair> getPieces() {
        ArrayList<IndexKeyPair> toReturn = new ArrayList<>();

        for (Share share : this.shares) {
            toReturn.add(new IndexKeyPair(share.getX(), Base64.getEncoder().encodeToString(share.getYValues())));
        }

        return toReturn;
    }

    @Override
    public String getAlgorithm() {
        return engine.toString();
    }

    @Override
    public void setShares(Object shares) {
        this.shares = (Share[]) shares;
    }

}
