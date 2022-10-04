package com.ifsc.secstor.api.service;

import com.at.archistar.crypto.CSSEngine;
import com.at.archistar.crypto.KrawczykEngine;
import com.at.archistar.crypto.PSSEngine;
import com.at.archistar.crypto.ShamirEngine;
import com.at.archistar.crypto.data.InvalidParametersException;
import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.at.archistar.crypto.secretsharing.WeakSecurityException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifsc.secstor.api.advice.exception.ValidationException;
import com.ifsc.secstor.api.config.SecstorConfig;
import com.ifsc.secstor.api.dto.ReconstructDTO;
import com.ifsc.secstor.api.dto.SplitDTO;
import com.ifsc.secstor.api.model.*;
import com.ifsc.secstor.facade.*;
import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;
import com.ufsc.das.gcseg.secretsharing.SecretShareEngine;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static com.ifsc.secstor.api.advice.messages.ErrorMessages.*;
import static com.ifsc.secstor.api.advice.paths.Paths.SECRET_SHARING_BASE_AND_RECONSTRUCT;
import static com.ifsc.secstor.api.advice.paths.Paths.SECRET_SHARING_BASE_AND_SPLIT;
import static com.ifsc.secstor.api.util.Constants.*;

@Component
public class SecretSharingImplementation implements SecretSharingService {
    private final Engine shamir;
    private final Engine pss;
    private final Engine css;
    private final Engine krawczyk;
    private final Engine pvss;

    public SecretSharingImplementation(SecstorConfig config) throws WeakSecurityException, NoSuchAlgorithmException {
        this.shamir = new ArchistarEngine(new ShamirEngine(config.n(), config.k()), config);
        this.pss = new ArchistarEngine(new PSSEngine(config.n(), config.k()), config);
        this.css = new ArchistarEngine(new CSSEngine(config.n(), config.k()), config);
        this.krawczyk = new ArchistarEngine(new KrawczykEngine(config.n(), config.k()), config);
        this.pvss = new PVSSEngine(new SecretShareEngine(config.n(), config.k()), config);
    }

    @Override
    public Object split(SplitDTO splitDTO) throws UnsupportedEncodingException, InvalidVSSScheme {
        if (splitDTO.getData() == null || splitDTO.getData() == "")
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    NULL_DATA, SECRET_SHARING_BASE_AND_SPLIT);

        Object data;

        try {
            JSONObject baseObject = new JSONObject(splitDTO);
            data = baseObject.get(DATA);
        } catch (Exception exception) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    INVALID_DATA, SECRET_SHARING_BASE_AND_SPLIT);
        }

        if (splitDTO.getAlgorithm().equalsIgnoreCase(SHAMIR))
            return this.shamir.split(data.toString());

        if (splitDTO.getAlgorithm().equalsIgnoreCase(PSS))
            return this.pss.split(data.toString());

        if (splitDTO.getAlgorithm().equalsIgnoreCase(CSS))
            return this.css.split(data.toString());

        if (splitDTO.getAlgorithm().equalsIgnoreCase(KRAWCZYK))
            return this.krawczyk.split(data.toString());

        if (splitDTO.getAlgorithm().equalsIgnoreCase(PVSS))
            return this.pvss.split(data.toString());

        throw new ValidationException(HttpStatus.BAD_REQUEST,
                INVALID_ALGORITHM, SECRET_SHARING_BASE_AND_SPLIT);
    }

    @Override
    public String reconstruct(ReconstructDTO reconstructDTO) throws UnsupportedEncodingException,
            InvalidParametersException, InvalidVSSScheme, ReconstructionException, JsonProcessingException {

        if (reconstructDTO == null)
            throw new ValidationException(HttpStatus.BAD_REQUEST, NULL_SECRET,
                    SECRET_SHARING_BASE_AND_RECONSTRUCT);

        JSONObject base;
        JSONObject secret;
        boolean doYourBest;

        try {
            base = new JSONObject(reconstructDTO);
            secret = base.getJSONObject(SECRET);
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    INVALID_SECRET, SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }

        try {
            doYourBest = base.getJSONObject(PARAMETERS).getBoolean(DOYOURBEST);
        } catch (Exception e) {
            doYourBest = false;
        }

        Object requestObject;

        if (secret.has(MACKEYS)) {
            requestObject = mapObject(secret, PSSShareModel.class);
            return this.pss.reconstruct(requestObject, doYourBest);
        } else if (secret.has(FINGERPRINTS)) {
            requestObject = mapObject(secret, CSSShareModel.class);
            return this.css.reconstruct(requestObject, doYourBest);
        } else if (secret.has(ENCKEYS)) {
            requestObject = mapObject(secret, KrawczykShareModel.class);
            return this.krawczyk.reconstruct(requestObject, doYourBest);
        } else if (secret.has(MODULUS)) {
            requestObject = mapObject(secret, PVSSShareModel.class);
            return this.pvss.reconstruct(requestObject, doYourBest);
        } else if (secret.has(SHARES)) {
            requestObject = mapObject(secret, ShamirShareModel.class);
            return this.shamir.reconstruct(requestObject, doYourBest);
        }

        throw new ValidationException(HttpStatus.BAD_REQUEST,
                NO_MATCH_SECRET, SECRET_SHARING_BASE_AND_RECONSTRUCT);
    }

    private <T> Object mapObject(JSONObject requestSecret, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(requestSecret.toString(), clazz);
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    e.getMessage(), SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }
    }
}
