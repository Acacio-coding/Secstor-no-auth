package com.ifsc.secstor.api.service;

import com.at.archistar.crypto.data.InvalidParametersException;
import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ifsc.secstor.api.dto.ReconstructDTO;
import com.ifsc.secstor.api.dto.SplitDTO;
import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public interface SecretSharingService {

    Object split(SplitDTO splitDTO) throws UnsupportedEncodingException, InvalidVSSScheme;
    String reconstruct(ReconstructDTO reconstructDTO) throws UnsupportedEncodingException, InvalidParametersException, InvalidVSSScheme, ReconstructionException, JsonProcessingException, IllegalAccessException;
}
