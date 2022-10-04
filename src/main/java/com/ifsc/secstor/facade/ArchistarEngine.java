package com.ifsc.secstor.facade;

import com.at.archistar.crypto.*;
import com.at.archistar.crypto.data.*;
import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.ifsc.secstor.api.advice.exception.ReconstructException;
import com.ifsc.secstor.api.advice.exception.ValidationException;
import com.ifsc.secstor.api.config.SecstorConfig;
import com.ifsc.secstor.api.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.ifsc.secstor.api.advice.messages.ErrorMessages.*;
import static com.ifsc.secstor.api.advice.paths.Paths.SECRET_SHARING_BASE_AND_RECONSTRUCT;
import static com.ifsc.secstor.api.util.Constants.*;

@RequiredArgsConstructor
@Component
public class ArchistarEngine implements Engine {
    private final CryptoEngine engine;

    private final SecstorConfig config;

    @Override
    public Object split(String data) {
        Share[] shares = engine.share(data.getBytes(StandardCharsets.UTF_8));

        Object toReturn;

        if (engine.toString().contains("shamir")) {
            toReturn = new ShamirShareModel(new ArrayList<>(), shares[0].getOriginalLength());
            fillShamirShareModel(shares, (ShamirShareModel) toReturn);
        } else if (engine.toString().contains("pss")) {
            toReturn = new PSSShareModel(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    shares[0].getOriginalLength());
            fillPSSShareModel(shares, (PSSShareModel) toReturn);
        } else if (engine.toString().contains("css")) {
            toReturn = new CSSShareModel(new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), shares[0].getOriginalLength(), ((CSSShare)shares[0]).getEncAlgorithm());
            fillCSSShareModel(shares, (CSSShareModel) toReturn);
        } else {
            toReturn = new KrawczykShareModel(new ArrayList<>(), new ArrayList<>(), shares[0].getOriginalLength(),
                    ((KrawczykShare)shares[0]).getEncAlgorithm());
            fillKrawczykShareModel(shares, (KrawczykShareModel) toReturn);
        }

        return toReturn;
    }

    private void fillShamirShareModel(Share[] generatedShares, ShamirShareModel toReturn) {
        for (Share share : generatedShares) {
            toReturn.getShares()
                    .add(new IndexKeyPair(share.getX(), Base64.encodeBase64String(share.getYValues())));
        }
    }

    private void fillPSSShareModel(Share[] generatedShares, PSSShareModel toReturn) {
        for (Share share : generatedShares) {
            toReturn.getShares()
                    .add(new IndexKeyPair(share.getX(), Base64.encodeBase64String(share.getYValues())));


            List<IndexKeyPair> auxMKeys = new ArrayList<>();
            List<IndexKeyPair> auxMacs = new ArrayList<>();

            int j = 1;
            for (int i = 0; i < generatedShares.length; i++) {
                auxMKeys.add(new IndexKeyPair(j,
                        Base64.encodeBase64String(((PSSShare) share).getMacKeys().get((byte) j))));

                auxMacs.add(new IndexKeyPair(j,
                        Base64.encodeBase64String(((PSSShare) share).getMacs().get((byte) j))));
                j++;
            }

            toReturn.getMacKeys().add(new IndexArrayPair(share.getX(), auxMKeys));
            toReturn.getMacs().add(new IndexArrayPair(share.getX(), auxMacs));
        }
    }

    private void fillCSSShareModel(Share[] generatedShares, CSSShareModel toReturn) {
        int i = 1;
        for (Share share : generatedShares) {
            toReturn.getShares()
                    .add(new IndexKeyPair(share.getX(), Base64.encodeBase64String(share.getYValues())));

            toReturn.getFingerprints().
                    add(new IndexKeyPair(i, Base64.encodeBase64String(((CSSShare) share).getFingerprints()
                            .get((byte) i))));

            toReturn.getEncKeys()
                    .add(new IndexKeyPair(i, Base64.encodeBase64String(((CSSShare) share).getKey())));

            i++;
        }
    }

    private void fillKrawczykShareModel(Share[] generatedShares, KrawczykShareModel toReturn) {
        for (Share share : generatedShares) {
            toReturn.getShares().add(new IndexKeyPair(share.getX(),
                    Base64.encodeBase64String(share.getYValues())));

            toReturn.getEncKeys().add(new IndexKeyPair(share.getX(),
                    Base64.encodeBase64String(((KrawczykShare) share).getKey())));
        }
    }

    @Override
    public String reconstruct(Object requestDTO, boolean doYourBest) throws ReconstructionException {
        Share[] shares = new Share[0];

        if (engine.toString().contains("shamir")) {
            shares = createShamirShares(doYourBest, (ShamirShareModel) requestDTO);
        } 
        if (engine.toString().contains("pss")) {
            assert requestDTO instanceof PSSShareModel;
            shares = createPSSShares(doYourBest, (PSSShareModel) requestDTO);
        } 

        if (engine.toString().contains("css")) {
            assert requestDTO instanceof CSSShareModel;
            shares = createCSSShares(doYourBest, (CSSShareModel) requestDTO);
        } 
        
        if (engine.toString().contains("krawczyk")) {
            assert requestDTO instanceof KrawczykShareModel;
            shares = createKrawczykShares(doYourBest, (KrawczykShareModel) requestDTO);
        }

        try {
            ReconstructionResult result = engine.reconstruct(shares);
            return new String(result.getData(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    e.getMessage().replaceAll("\\n", ". "), SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }
    }

    private Share[] createShamirShares(boolean doYourBest, ShamirShareModel requestSecret)  {
        List<Share> shareList = new ArrayList<>();

        validateAllIndexAndKey(requestSecret.getShares(), SHARE);
        validateOriginalLength(requestSecret.getOriginalLength());

        List<Integer> validIndexes = getIndexes(requestSecret.getShares());

        for (int i = 0; i < requestSecret.getShares().size(); i++) {
            IndexKeyPair currentKeyObject = requestSecret.getShares().get(i);

            lookForEqualIndexesInTheSameList(validIndexes, currentKeyObject.index());

            try {
                Share current = new ShamirShare((byte) currentKeyObject.index(),
                        Base64.decodeBase64(currentKeyObject.key()));

                validateParameterByOriginalLength(current.getOriginalLength(),
                        requestSecret.getOriginalLength(), i + 1);

                if (!shareList.contains(current))
                    shareList.add(current);
            } catch (Exception e) {
                if (!doYourBest) {
                    throw new ReconstructException(HttpStatus.BAD_REQUEST,
                            INVALID_KEY(i + 1) + e.getMessage(),
                            currentKeyObject.index(), currentKeyObject.key(), SHARE,
                            SECRET_SHARING_BASE_AND_RECONSTRUCT);
                } else {
                    requestSecret.getShares().remove(i);
                    validateAllIndexAndKey(requestSecret.getShares(), SHARE);
                    i--;
                }
            }
        }

        return createShareArray(shareList);
    }

    private Share[] createPSSShares(boolean doYourBest, PSSShareModel requestSecret) {
        List<Share> shareList = new ArrayList<>();

        validateAllIndexAndKey(requestSecret.getShares(), SHARE);
        validateAllIndexAndArray(requestSecret.getMacKeys(), MACKEY, INNER_MACKEY);
        validateAllIndexAndArray(requestSecret.getMacKeys(), MAC, INNER_MAC);

        validateOriginalLength(requestSecret.getOriginalLength());

        validateSizes(requestSecret.getShares(), requestSecret.getMacKeys(), PSS);
        validateSizes(requestSecret.getShares(), requestSecret.getMacs(), PSS);

        List<Integer> validIndexes = getIndexes(requestSecret.getShares());

        List<Integer> outerMacKeyIndexes = getIndexesOfArrayPair(requestSecret.getMacKeys());
        validateIndexesByValidList(validIndexes, outerMacKeyIndexes, PSS);

        List<Integer> outerMacIndexes = getIndexesOfArrayPair(requestSecret.getMacs());
        validateIndexesByValidList(validIndexes, outerMacIndexes, PSS);

        for (int i = 0; i < requestSecret.getMacKeys().size(); i++) {
            List<Integer> innerMacKeyIndexes = getIndexes(requestSecret.getMacKeys().get(i).array());
            validateIndexesByValidList(validIndexes, innerMacKeyIndexes, PSS);

            List<Integer> innerMacIndexes = getIndexes(requestSecret.getMacs().get(i).array());
            validateIndexesByValidList(validIndexes, innerMacIndexes, PSS);
        }

        for (int i = 0; i < requestSecret.getShares().size(); i++) {
            Map<Byte, byte[]> macKeys = new HashMap<>();
            Map<Byte, byte[]> macs = new HashMap<>();

            IndexKeyPair currentKeyObject = requestSecret.getShares().get(i);

            lookForEqualIndexesInTheSameList(validIndexes, currentKeyObject.index());

            IndexArrayPair currentMacKeyObject = requestSecret.getMacKeys().get(i);
            List<IndexKeyPair> currentMacKeyArray = currentMacKeyObject.array();

            IndexArrayPair currentMacObject = requestSecret.getMacs().get(i);
            List<IndexKeyPair> currentMacArray = currentMacObject.array();

            int k = requestSecret.getShares().get(0).index();
            for (int j = 0; j < requestSecret.getShares().size(); j++) {
                IndexKeyPair currentMacKey = currentMacKeyArray.get(j);
                IndexKeyPair currentMac = currentMacArray.get(j);

                lookForEqualIndexesInTheSameList(getIndexes(currentMacKeyArray), currentMacKey.index());
                lookForEqualIndexesInTheSameList(getIndexes(currentMacArray), currentMac.index());

                if (currentMacKey.index() == k && currentMac.index() == k) {
                    try {
                        if (!macKeys.containsKey((byte) k)
                                && !macKeys.containsValue(Base64.decodeBase64(currentMacKey.key())))
                            macKeys.put((byte) k, Base64.decodeBase64(currentMacKey.key()));
                    } catch (Exception e) {
                        throw new ReconstructException(HttpStatus.BAD_REQUEST,
                                e.getMessage(), currentMacKey.index(), currentMacKey.key(),
                                MACKEY, SECRET_SHARING_BASE_AND_RECONSTRUCT);
                    }

                    try {
                        if (!macs.containsKey((byte) k)
                                && !macs.containsValue(Base64.decodeBase64(currentMac.key())))
                            macs.put((byte) k, Base64.decodeBase64(currentMac.key()));
                    } catch (Exception e) {
                        throw new ReconstructException(HttpStatus.BAD_REQUEST,
                                e.getMessage(), currentMac.index(), currentMac.key(),
                                MAC, SECRET_SHARING_BASE_AND_RECONSTRUCT);
                    }

                    k++;
                } else {
                    k = currentMacKey.index();
                    j--;
                }
            }

            try {
                Share current = new PSSShare((byte) currentKeyObject.index(),
                        Base64.decodeBase64(currentKeyObject.key()), macKeys, macs);

                validateParameterByOriginalLength(current.getOriginalLength(),
                        requestSecret.getOriginalLength(), i + 1);

                if (!shareList.contains(current))
                    shareList.add(current);
            } catch (Exception e) {
                if (!doYourBest) {
                    throw new ReconstructException(HttpStatus.BAD_REQUEST,
                            e.getMessage(), currentKeyObject.index(), currentKeyObject.key(),
                            SHARE, SECRET_SHARING_BASE_AND_RECONSTRUCT);
                } else {
                    requestSecret.getMacKeys().remove(i);
                    requestSecret.getMacs().remove(i);
                    requestSecret.getShares().remove(i);
                    validIndexes = getIndexes(requestSecret.getShares());
                    reorganizeIndexArrayPairList(requestSecret.getMacKeys(), validIndexes);
                    reorganizeIndexArrayPairList(requestSecret.getMacs(), validIndexes);
                    validateAllIndexAndKey(requestSecret.getShares(), SHARE);
                    validateParameterWithNestedArray(requestSecret.getMacKeys(), MACKEY, INNER_MACKEY);
                    validateParameterWithNestedArray(requestSecret.getMacs(), MAC, INNER_MAC);
                    i--;
                }
            }
        }

        return createShareArray(shareList);
    }

    private Share[] createCSSShares(boolean doYourBest, CSSShareModel requestSecret) {
        List<Share> shareList = new ArrayList<>();

        validateSizes(requestSecret.getShares(), requestSecret.getFingerprints(), FINGERPRINT);
        validateSizes(requestSecret.getShares(), requestSecret.getEncKeys(), ENCKEY);
        validateAllIndexAndKey(requestSecret.getShares(), SHARE);
        validateAllIndexAndKey(requestSecret.getFingerprints(), FINGERPRINT);
        validateAllIndexAndKey(requestSecret.getEncKeys(), ENCKEY);
        validateEncAlgorithm(requestSecret.getEncAlgorithm());
        validateOriginalLength(requestSecret.getOriginalLength());

        List<Integer> validIndexes = getIndexes(requestSecret.getShares());
        validateIndexesByValidList(validIndexes, getIndexes(requestSecret.getFingerprints()), CSS);
        validateIndexesByValidList(validIndexes, getIndexes(requestSecret.getEncKeys()), CSS);

        Map<Byte, byte[]> fingerprints = new HashMap<>();

        int j = requestSecret.getShares().get(0).index();
        for (int i = 0; i < requestSecret.getShares().size(); i++) {
            IndexKeyPair currentFingerprintObject = requestSecret.getFingerprints().get(i);

            lookForEqualIndexesInTheSameList(getIndexes(requestSecret.getFingerprints()),
                    currentFingerprintObject.index());

            if (currentFingerprintObject.index() == j) {
                try {
                    fingerprints.put((byte) j, Base64.decodeBase64(currentFingerprintObject.key()));
                } catch (Exception e) {
                    throw new ReconstructException(HttpStatus.BAD_REQUEST,
                            e.getMessage(), currentFingerprintObject.index(),
                            currentFingerprintObject.key(), FINGERPRINT, SECRET_SHARING_BASE_AND_RECONSTRUCT);
                }

                j++;
            } else {
                j = currentFingerprintObject.index();
                i--;
            }
        }

        for (int i = 0; i < requestSecret.getShares().size(); i++) {
            IndexKeyPair currentKeyObject = requestSecret.getShares().get(i);
            IndexKeyPair currentEncKeyObject = requestSecret.getEncKeys().get(i);
            byte[] currentEncKeyDecode;

            lookForEqualIndexesInTheSameList(validIndexes, currentKeyObject.index());
            lookForEqualIndexesInTheSameList(getIndexes(requestSecret.getEncKeys()), currentEncKeyObject.index());

            try {
                currentEncKeyDecode = Base64.decodeBase64(currentEncKeyObject.key());
            } catch (Exception e) {
                throw new ReconstructException(HttpStatus.BAD_REQUEST,
                        e.getMessage(), currentEncKeyObject.index(),
                        currentEncKeyObject.key(), ENCKEY, SECRET_SHARING_BASE_AND_RECONSTRUCT);
            }

            try {
                Share current = new CSSShare((byte) currentKeyObject.index(),
                        Base64.decodeBase64(currentKeyObject.key()), fingerprints,
                        requestSecret.getOriginalLength(), requestSecret.getEncAlgorithm(),
                        currentEncKeyDecode);

                validateParameterByOriginalLength(current.getOriginalLength(),
                        requestSecret.getOriginalLength(), i + 1);

                if (!shareList.contains(current))
                    shareList.add(current);
            } catch (Exception e) {
                if (!doYourBest) {
                    throw new ReconstructException(HttpStatus.BAD_REQUEST,
                            e.getMessage(), currentKeyObject.index(), currentKeyObject.key(),
                            SHARE, SECRET_SHARING_BASE_AND_RECONSTRUCT);
                } else {
                    requestSecret.getShares().remove(i);
                    validateAllIndexAndKey(requestSecret.getShares(), SHARE);
                    requestSecret.getFingerprints().remove(i);
                    validateAllIndexAndKey(requestSecret.getFingerprints(), FINGERPRINT);
                    requestSecret.getEncKeys().remove(i);
                    validateAllIndexAndKey(requestSecret.getEncKeys(), ENCKEY);
                    validIndexes = getIndexes(requestSecret.getShares());
                    reorganizeMap(fingerprints, validIndexes);
                    i--;
                }
            }
        }

        return createShareArray(shareList);
    }

    private Share[] createKrawczykShares(boolean doYourBest, KrawczykShareModel requestSecret) {
        List<Share> shareList = new ArrayList<>();

        validateSizes(requestSecret.getShares(), requestSecret.getEncKeys(), ENCKEY);
        validateAllIndexAndKey(requestSecret.getShares(), SHARE);
        validateAllIndexAndKey(requestSecret.getEncKeys(), ENCKEY);
        validateEncAlgorithm(requestSecret.getEncAlgorithm());
        validateOriginalLength(requestSecret.getOriginalLength());

        List<Integer> validIndexes = getIndexes(requestSecret.getShares());
        validateIndexesByValidList(validIndexes, getIndexes(requestSecret.getEncKeys()), ENCKEY);

        for (int i = 0; i < requestSecret.getShares().size(); i++) {
            IndexKeyPair currentKeyObject = requestSecret.getShares().get(i);
            IndexKeyPair currentEncKeyObject = requestSecret.getEncKeys().get(i);
            byte[] currentEncKeyDecode;

            lookForEqualIndexesInTheSameList(validIndexes, currentKeyObject.index());
            lookForEqualIndexesInTheSameList(getIndexes(requestSecret.getEncKeys()), currentEncKeyObject.index());

            try {
                currentEncKeyDecode = Base64.decodeBase64(currentEncKeyObject.key());
            } catch (Exception e) {
                throw new ReconstructException(HttpStatus.BAD_REQUEST,
                        e.getMessage(), currentEncKeyObject.index(),
                        currentEncKeyObject.key(), ENCKEY, SECRET_SHARING_BASE_AND_RECONSTRUCT);
            }

            try {
                Share current = new KrawczykShare((byte) currentKeyObject.index(),
                        Base64.decodeBase64(currentKeyObject.key()), requestSecret.getOriginalLength(),
                        requestSecret.getEncAlgorithm(), currentEncKeyDecode);

                validateParameterByOriginalLength(current.getOriginalLength(),
                        requestSecret.getOriginalLength(), i + 1);

                if (!shareList.contains(current))
                    shareList.add(current);
            } catch (Exception e) {
                if (!doYourBest) {
                    throw new ReconstructException(HttpStatus.BAD_REQUEST,
                            e.getMessage(), currentKeyObject.index(),
                            currentKeyObject.key(), SHARE, SECRET_SHARING_BASE_AND_RECONSTRUCT);
                } else {
                    requestSecret.getShares().remove(i);
                    validateAllIndexAndKey(requestSecret.getShares(), SHARE);
                    requestSecret.getEncKeys().remove(i);
                    validateAllIndexAndKey(requestSecret.getEncKeys(), ENCKEY);
                    i--;
                }
            }
        }

        return createShareArray(shareList);
    }

    private Share[] createShareArray(List<Share> shareList) {
        Share[] shareArray = new Share[shareList.size()];

        for (int i = 0; i < shareList.size(); i++) {
            shareArray[i] = shareList.get(i);
        }

        return shareArray;
    }

    private void validateAllIndexAndKey(List<IndexKeyPair> keyObjectList, String type) {
        if (keyObjectList.size() > config.n()) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    OUT_OF_BOUNDS_PARAMETER(type), SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }

        if (keyObjectList.size() < config.k()) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    INVALID_PARAMETER_SIZE(type), SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }

        int i = 1;
        for (IndexKeyPair currentKeyObject : keyObjectList) {
            if (currentKeyObject.index() < 1 || currentKeyObject.index() > 10)
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        INVALID_INDEX(i, type, currentKeyObject.index()), SECRET_SHARING_BASE_AND_RECONSTRUCT);

            if (currentKeyObject.key().isBlank())
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        EMPTY_PARAMETER(i, type), SECRET_SHARING_BASE_AND_RECONSTRUCT);

            i++;
        }
    }

    private void validateAllIndexAndArray(List<IndexArrayPair> keyObjectList, String type, String innerType) {
        int i = 1;
        for (IndexArrayPair currentKeyObject : keyObjectList) {
            if (currentKeyObject.index() < 1 || currentKeyObject.index() > 10)
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        INVALID_INDEX(i, type, currentKeyObject.index()), SECRET_SHARING_BASE_AND_RECONSTRUCT);

            for (int j = 0; j < currentKeyObject.array().size(); j++) {
                IndexKeyPair currentInnerKeyObject = currentKeyObject.array().get(j);

                if (currentInnerKeyObject.index() < 1 || currentInnerKeyObject.index() > 10)
                    throw new ValidationException(HttpStatus.BAD_REQUEST,
                            INVALID_INDEX(j + 1, innerType, currentInnerKeyObject.index()),
                            SECRET_SHARING_BASE_AND_RECONSTRUCT);

                if (currentInnerKeyObject.key().isBlank())
                    throw new ValidationException(HttpStatus.BAD_REQUEST,
                            EMPTY_PARAMETER(j + 1, innerType), SECRET_SHARING_BASE_AND_RECONSTRUCT);
            }

            if (currentKeyObject.array().size() < 1)
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        NULL_PARAMETER(type), SECRET_SHARING_BASE_AND_RECONSTRUCT);

            if (currentKeyObject.array().size() > 10)
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        OUT_OF_BOUNDS_PARAMETER(type), SECRET_SHARING_BASE_AND_RECONSTRUCT);

            i++;
        }
    }

    private void validateSizes(List<?> validSize, List<?> toValidate, String algorithm) {
        if (validSize.size() != toValidate.size()) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    INVALID_SIZES(algorithm), SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }
    }

    private List<Integer> getIndexes(List<IndexKeyPair> list) {
        List<Integer> indexes = new ArrayList<>();

        for (IndexKeyPair indexKeyPair : list) {
            indexes.add(indexKeyPair.index());
        }

        return indexes;
    }

    private List<Integer> getIndexesOfArrayPair(List<IndexArrayPair> list) {
        List<Integer> indexes = new ArrayList<>();

        for (IndexArrayPair indexKeyPair : list) {
            indexes.add(indexKeyPair.index());
        }

        return indexes;
    }

    private void lookForEqualIndexesInTheSameList(List<Integer> indexes, int toCheck) {
        int equalCount = 0;

        for (Integer index : indexes) {
            if (index == toCheck)
                equalCount++;
        }

        if (equalCount > 1)
            throw new ValidationException(HttpStatus.BAD_REQUEST, EQUAL_INDEXES,
                    SECRET_SHARING_BASE_AND_RECONSTRUCT);
    }

    private void validateParameterWithNestedArray(List<IndexArrayPair> parameterList, String type, String innerType) {
        if (parameterList.isEmpty())
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    NULL_PARAMETER(type), SECRET_SHARING_BASE_AND_RECONSTRUCT);

        if (parameterList.size() < config.k())
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    NOT_ENOUGH_PARAMETERS(type), SECRET_SHARING_BASE_AND_RECONSTRUCT);

        for (IndexArrayPair indexArrayPair : parameterList) {
            List<IndexKeyPair> innerArray = indexArrayPair.array();

            if (innerArray.isEmpty())
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        NULL_PARAMETER(innerType), SECRET_SHARING_BASE_AND_RECONSTRUCT);

            if (innerArray.size() < config.k())
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        NOT_ENOUGH_PARAMETERS(innerType), SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }
    }

    private void validateParameterByOriginalLength(int parameterLength, int originalLength, int index) {
        if (parameterLength != originalLength)
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    NOT_MATCHING_LENGTH(com.ifsc.secstor.api.util.Constants.SHARE, index), SECRET_SHARING_BASE_AND_RECONSTRUCT);
    }

    private void validateIndexesByValidList(List<Integer> validList,
                                            List<Integer> toValidate, String algorithm) {
        for (Integer index : toValidate) {
            if (validList.stream().noneMatch(current -> Objects.equals(current, index))) {
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        INVALID_INDEXES(algorithm), SECRET_SHARING_BASE_AND_RECONSTRUCT);
            }
        }
    }

    private void reorganizeIndexArrayPairList(List<IndexArrayPair> toReorganize, List<Integer> indexes) {
        for (IndexArrayPair currentObject : toReorganize) {
            List<IndexKeyPair> innerList = currentObject.array();

            Iterator<IndexKeyPair> iterator = innerList.listIterator();
            while (iterator.hasNext()) {
                if (indexes.stream().noneMatch(index -> index == currentObject.index())) {
                    iterator.remove();
                }
            }
        }
    }

    private void reorganizeMap(Map<Byte, byte[]> map, List<Integer> indexes) {
        map.entrySet().removeIf(entry -> indexes.stream().noneMatch(index -> index == entry.getKey().intValue()));
    }

    private void validateEncAlgorithm(int encAlgorithm) {
        if (encAlgorithm <= 0) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, INVALID_ENCALGORITHM,
                    SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }
    }

    private void validateOriginalLength(int originalLength) {
        if (originalLength <= 0) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, INVALID_ORIGINALLENGTH,
                    SECRET_SHARING_BASE_AND_RECONSTRUCT);
        }
    }
}