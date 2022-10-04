package com.ifsc.secstor.facade;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.at.archistar.crypto.data.InvalidParametersException;
import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.ifsc.secstor.api.advice.exception.ValidationException;
import com.ifsc.secstor.api.model.IndexKeyPair;
import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import static com.ifsc.secstor.api.advice.messages.ErrorMessages.*;
import static com.ifsc.secstor.api.advice.paths.Paths.SECRET_SHARING_BASE_AND_RECONSTRUCT;

public interface Engine {
    Object split(String data) throws UnsupportedEncodingException, InvalidVSSScheme;
    String reconstruct(Object requestDTO, boolean doYourBest) throws UnsupportedEncodingException,
            InvalidVSSScheme, ReconstructionException;
}
