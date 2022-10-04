package com.ifsc.secstor.utils.facade;

import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.ifsc.secstor.utils.IndexKeyPair;
import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public interface Engine {
    void split(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidVSSScheme, com.ifsc.secstor.utils.pvss.InvalidVSSScheme;

    String reconstruct() throws UnsupportedEncodingException, InvalidVSSScheme, ReconstructionException, com.ifsc.secstor.utils.pvss.InvalidVSSScheme;

    ArrayList<IndexKeyPair> getPieces();

    String getAlgorithm();

    void setShares(Object shares);
}
