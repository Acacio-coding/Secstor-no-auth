package com.ifsc.secstor.api.service;

import com.ifsc.secstor.api.advice.exception.ValidationException;
import com.ifsc.secstor.api.dto.AnonymizationDTO;
import com.ifsc.secstor.facade.GeneralizatorUtil;
import com.ifsc.secstor.facade.RandomizerUtil;
import com.ifsc.secstor.facade.SuppressorUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.ifsc.secstor.api.advice.messages.ErrorMessages.*;
import static com.ifsc.secstor.api.advice.paths.Paths.*;
import static com.ifsc.secstor.api.util.Constants.*;

@Component
public class AnonymizationServiceImplementation implements AnonymizationService {

    @Override
    public List<Object> anonymize(AnonymizationDTO anonymizationDTO) {
        nullSafetyValidation(anonymizationDTO);

        JSONObject baseObject = jsonBaseObjectCast(anonymizationDTO);

        //Extracting data array from base object
        JSONArray data = jsonArrayCast(baseObject.get(DATA));

        //Extracting config objects from base object
        JSONObject attribute_config = jsonObjectCast(baseObject.get(ATTRIBUTE_CONFIG));

        if (data.length() == 1) {
            return suppressEverything(data);
        }

        //Request validations
        keySetValidation(data, attribute_config);
        parameterValidation(attribute_config);
        classificationValidation(attribute_config);

        //Filtering attribute classifications
        List<String> identifiers = filterIdentifier(attribute_config);
        List<String> sensitives = filterSensitive(attribute_config);

        //Suppress identifiers
        suppressIdentifiers(data, identifiers);

        //Randomize sensitives
        randomizeSensitives(data, sensitives
                .stream()
                .filter(parameter -> attribute_config.getJSONObject(parameter).get(METHOD).equals(RANDOMIZATION))
                .toList());

        //Generalize Sensitives
        //Extracting level config from base object
        if (baseObject.has(GENERALIZATION_LEVEL)) {
            int generalization_level = getLevel(baseObject);

            generalizeSensitivesWithLevel(data, sensitives
                    .stream()
                    .filter(parameter -> attribute_config.getJSONObject(parameter).get(METHOD).equals(GENERALIZATION))
                    .toList(),
                    generalization_level);
        } else {
            generalizeSensitives(data, sensitives
                    .stream()
                    .filter(parameter -> attribute_config.getJSONObject(parameter).get(METHOD).equals(GENERALIZATION))
                    .toList());
        }

        return data.toList();
    }

    private void generalizeSensitives(JSONArray data, List<String> fields) {
        for (String field : fields) {
            List<Object> valuesList = getList(data, field);

            List<String> generalizedData = GeneralizatorUtil.receiveData(valuesList, field);

            putBackInfo(data, field, generalizedData);
        }
    }

    private void generalizeSensitivesWithLevel(JSONArray data, List<String> fields, int level) {
        for (String field : fields) {
            List<Object> valuesList = getList(data, field);

            List<String> generalizedData = GeneralizatorUtil.receiveDataWithLevel(valuesList, field, level);

            putBackInfo(data, field, generalizedData);
        }
    }

    private JSONObject jsonBaseObjectCast(Object property) {
        try {
            return new JSONObject(property);
        } catch (Exception exception) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, INVALID_BODY,
                    DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
        }
    }

    private JSONObject jsonObjectCast(Object property) {
        try {
            return (JSONObject) property;
        } catch (Exception exception) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, INVALID_ATTRIBUTE_CONFIG,
                    DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
        }
    }

    private JSONArray jsonArrayCast(Object property) {
        try {
            return (JSONArray) property;
        } catch (Exception exception) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, INVALID_DATA_ARRAY,
                    DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
        }
    }

    private void nullSafetyValidation(AnonymizationDTO anonymizationDTO) {
        if (anonymizationDTO == null)
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    NULL_BODY, DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);

        if (anonymizationDTO.getData() == null)
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    NULL_DATA, DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);

        if (anonymizationDTO.getAttribute_config() == null)
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    NULL_ATTRIBUTE_CONFIG, DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
    }

    private void classificationValidation(JSONObject attribute_config) {
        for (String parameter : attribute_config.keySet()) {
            JSONObject currentObject = attribute_config.getJSONObject(parameter);

            if(!currentObject.has(CLASSIFICATION))
                throw new ValidationException(HttpStatus.BAD_REQUEST, NULL_CLASSIFICATION,
                        DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);

            Object classification = currentObject.get(CLASSIFICATION);

            if (!classification.equals(IDENTIFIER) && !classification.equals(SENSITIVE)
                    && !classification.equals(QUASI_IDENTIFIER))
                throw new ValidationException(HttpStatus.BAD_REQUEST, INVALID_CLASSIFICATION(parameter),
                        DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);

            if (classification.equals(SENSITIVE)) {
                if (!currentObject.has(METHOD)) {
                    throw new ValidationException(HttpStatus.BAD_REQUEST, NULL_METHOD(parameter),
                            DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
                }

                if (!currentObject.get(METHOD).equals(GENERALIZATION)
                        && !currentObject.get(METHOD).equals(RANDOMIZATION)) {
                    throw new ValidationException(HttpStatus.BAD_REQUEST, INVALID_METHOD(parameter),
                            DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
                }
            }
        }
    }

    private void keySetValidation(JSONArray data, JSONObject attribute_config) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject currentObject = (JSONObject) data.get(i);

            if (!attribute_config.keySet().equals(currentObject.keySet()))
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        INVALID_KEYSET, DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
        }
    }

    private void parameterValidation(JSONObject attribute_config) {
        for (String parameter : attribute_config.keySet()) {
            try {
                attribute_config.getJSONObject(parameter);
            } catch (Exception exception) {
                throw new ValidationException(HttpStatus.BAD_REQUEST,
                        INVALID_PARAMETER(parameter), DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
            }
        }
    }

    private int getLevel(JSONObject baseObject) {
        try {
            return baseObject.getInt(GENERALIZATION_LEVEL);
        } catch (Exception exception) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    INVALID_GENERALIZATION_LEVEL, DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
        }
    }

    private List<Object> getList(JSONArray data, String field) {
        List<Object> toReturn = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            JSONObject currentObject = (JSONObject) data.get(i);
            toReturn.add(currentObject.get(field));
        }

        return toReturn;
    }

    private void putBackInfo(JSONArray data, String field, List<String> values) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject currentObject = (JSONObject) data.get(i);

            currentObject.put(field, values.get(i));

            data.put(i, currentObject);
        }
    }

    private List<String> filterIdentifier(JSONObject attribute_config) {
        return attribute_config.keySet()
                .stream()
                .filter(currentKey -> attribute_config
                        .getJSONObject(currentKey).get(CLASSIFICATION).equals(IDENTIFIER))
                .toList();
    }

    private List<String> filterSensitive(JSONObject attribute_config) {
        return attribute_config.keySet()
                .stream()
                .filter(currentKey -> attribute_config
                        .getJSONObject(currentKey).get(CLASSIFICATION).equals(SENSITIVE))
                .toList();
    }

    private List<Object> suppressEverything(JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject currentObjectAsJson = (JSONObject) data.get(i);

            for (String parameter : currentObjectAsJson.keySet()) {
                currentObjectAsJson.put(parameter, SuppressorUtil.suppress());
            }

            data.put(i, currentObjectAsJson);
        }

        return data.toList();
    }

    private void suppressIdentifiers(JSONArray data, List<String> fields) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject currentObject = data.getJSONObject(i);

            if (!fields.isEmpty()) {
                for (String parameter : fields) {
                    currentObject.put(parameter, SuppressorUtil.suppress());
                }
            }

            data.put(i, currentObject);
        }
    }

    private void randomizeSensitives(JSONArray data, List<String> fields) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject currentObject = data.getJSONObject(i);

            if (!fields.isEmpty()) {
                for (String parameter : fields) {
                    currentObject.put(parameter, RandomizerUtil.randomize(currentObject.get(parameter)));
                }
            }

            data.put(i, currentObject);
        }
    }
}
