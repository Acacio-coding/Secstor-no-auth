package com.ifsc.secstor.facade;

import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.ifsc.secstor.api.advice.exception.ValidationException;
import com.ifsc.secstor.api.config.SecstorConfig;
import com.ifsc.secstor.api.model.IndexKeyPair;
import com.ifsc.secstor.api.model.PVSSShareModel;
import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;
import com.ufsc.das.gcseg.secretsharing.SecretShareEngine;
import com.ufsc.das.gcseg.secretsharing.SharestoCombine;
import com.ufsc.das.gcseg.secretsharing.SplitedShares;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ifsc.secstor.api.advice.messages.ErrorMessages.*;
import static com.ifsc.secstor.api.advice.paths.Paths.SECRET_SHARING_BASE_AND_RECONSTRUCT;
import static com.ifsc.secstor.api.util.Constants.*;

@RequiredArgsConstructor
@Component
public class PVSSEngine implements Engine {
    private final SecretShareEngine engine;

    private final SecstorConfig config;

    @Override
    public Object split(String data) throws UnsupportedEncodingException, InvalidVSSScheme {
        SplitedShares splitedShares = engine.split(data);

        return new PVSSShareModel(
                splitedShares.getShareString(),
                splitedShares.getShareString().get(0).key().length(),
                splitedShares.getKey(),
                splitedShares.getKey().length(),
                splitedShares.getModulus(),
                splitedShares.getModulus().toString().length());
    }

    @Override
    public String reconstruct(Object requestDTO, boolean doYourBest) throws UnsupportedEncodingException, InvalidVSSScheme, ReconstructionException {
        SharestoCombine sharestoCombine;

        sharestoCombine = createPVSSShares(doYourBest, (PVSSShareModel) requestDTO);

        try {
            return this.engine.combine(sharestoCombine);
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    e.getMessage().replaceAll("\\n", ". "), SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }
    }

    private SharestoCombine createPVSSShares(boolean doYourBest, PVSSShareModel requestSecret) {
        if (!doYourBest) {
            validateOriginalLength(requestSecret.getKeyOriginalLength());
            validateOriginalLength(requestSecret.getModulusOriginalLength());
            validateParameter(requestSecret.getKey(), KEY, requestSecret.getKeyOriginalLength());
            validateParameter(requestSecret.getModulus().toString(), MODULUS, requestSecret.getModulusOriginalLength());
        }

        validateOriginalLength(requestSecret.getShareOriginalLength());
        validateAllIndexAndKey(requestSecret.getShares());
        validateIndexes(requestSecret.getShares());

        if (doYourBest) {
            requestSecret.getShares().removeIf(current -> !validateParameterWithIndex(current.key(), current.index(),
                    requestSecret.getShareOriginalLength(), true));

            validateAllIndexAndKey(requestSecret.getShares());
        } else {
            requestSecret.getShares().forEach(current -> validateParameterWithIndex(current.key(), current.index(),
                    requestSecret.getShareOriginalLength(), false));
        }

        SharestoCombine sharestoCombine = new SharestoCombine();
        sharestoCombine.setKey(requestSecret.getKey());
        sharestoCombine.setModulus(requestSecret.getModulus());

        requestSecret.getShares().forEach(sharestoCombine::addShare);

        return sharestoCombine;
    }

    private void validateAllIndexAndKey(List<IndexKeyPair> keyObjectList) {
        if (keyObjectList.size() > config.n()) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    OUT_OF_BOUNDS_PARAMETER(SHARE), SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }

        if (keyObjectList.size() < config.k()) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    INVALID_PARAMETER_SIZE(SHARE), SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }

        int i = 1;
        for (IndexKeyPair currentKeyObject : keyObjectList) {
            if (currentKeyObject.index() < 0 || currentKeyObject.index() > 10)
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        INVALID_INDEX(i, SHARE, currentKeyObject.index()), SECRET_SHARING_BASE_AND_RECONSTRUCT);

            if (currentKeyObject.key().isBlank())
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        EMPTY_PARAMETER(i, SHARE), SECRET_SHARING_BASE_AND_RECONSTRUCT);

            i++;
        }
    }

    private void validateOriginalLength(int originalLength) {
        if (originalLength <= 0)
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    INVALID_ORIGINALLENGTH, SECRET_SHARING_BASE_AND_RECONSTRUCT);
    }

    private boolean validateParameterWithIndex(String parameter, int index, int originalLength, boolean doYourBest) {
        if (parameter.isBlank()) {
            if (!doYourBest) {
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        EMPTY_PARAMETER(SHARE), SECRET_SHARING_BASE_AND_RECONSTRUCT);
            } else {
                return false;
            }
        }


        if (parameter.length() != originalLength) {
            if (!doYourBest) {
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        NOT_MATCHING_LENGTH(SHARE, index), SECRET_SHARING_BASE_AND_RECONSTRUCT);
            } else {
                return false;
            }
        }

        return true;
    }

    private void validateParameter(String parameter, String type, int originalLength) {
        if (parameter.isBlank())
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    EMPTY_PARAMETER(type), SECRET_SHARING_BASE_AND_RECONSTRUCT);

        if (parameter.length() != originalLength)
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    NOT_MATCHING_LENGTH(type), SECRET_SHARING_BASE_AND_RECONSTRUCT);
    }

    private void validateIndexes(List<IndexKeyPair> keyObjectList) {
        List<Integer> indexes = new ArrayList<>();

        for (IndexKeyPair current : keyObjectList) {
            indexes.add(current.index());
        }

        Collections.sort(indexes);

        for (int i = 0; i < indexes.size(); i++) {
            Integer current = indexes.get(i);
            Integer next = null;

            if ((i + 1) < keyObjectList.size())
                next = indexes.get((i + 1));

            if (next != null && (current + 1) != next) {
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        INVALID_INDEXES, SECRET_SHARING_BASE_AND_RECONSTRUCT);
            }
        }
    }
}