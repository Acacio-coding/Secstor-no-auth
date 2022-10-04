package com.ifsc.secstor.api.advice.messages;

public class ErrorMessages {

    public static final String NULL_BODY = "Body must be provided";
    public static final String INVALID_BODY = "Body provided is invalid, it must be an object";
    public static final String NULL_DATA = "Data must be provided";
    public static final String INVALID_DATA = "Data provided is invalid, it must be an object";
    public static final String INVALID_DATA_ARRAY = "Data provided is invalid, it must be an array of objects";
    public static final String INVALID_KEYSET = "Keysets of the objects are different";
    public static final String NULL_ALGORITHM = "Algorithm must be provided";
    public static final String INVALID_ALGORITHM = "Algorithm provided is invalid, it must be either SHAMIR, PSS, CSS, KRAWCZYK or PVSS";
    public static final String NULL_SECRET = "Secret must be provided";
    public static final String INVALID_SECRET = "Secret provided is invalid, it must be an object";
    public static final String INVALID_ORIGINALLENGTH = "Original length must be bigger than 0";
    public static final String NO_MATCH_SECRET = "Secret provided doesn't match any of share algorithm types";
    public static final String NULL_ATTRIBUTE_CONFIG = "Attribute config must be provided";
    public static final String INVALID_ATTRIBUTE_CONFIG = "Attribute config provided is invalid, it must be an object";
    public static final String INVALID_GENERALIZATION_STRING_LENGTH = "All strings must have the same length using generalization";
    public static final String INVALID_SIMILARITY_LEVEL = "In order to use generalization at least the first character of the strings must be equal";
    public static final String INVALID_GENERALIZATION_LEVEL = "Value provided for generalization level is invalid, it must be an integer number";
    public static final String NULL_CLASSIFICATION =  "Every parameter must have a classification, the options are: identifying, sensitive or quasi-identifier";
    public static String EQUAL_INDEXES = "All parameters must not have equal key indexes";
    public static String INVALID_ENCALGORITHM = "Encalgorithm is invalid, it must be bigger than 0";
    public static String INVALID_INDEXES = "In order to use PVSS algorithm for now, all share objects index must be part of a sequence like: 0 -> 1 -> 2 -> 3 -> 4 -> 5 " +
            "or 8 -> 7 -> 6 -> 5 -> 4 or even 5 -> 7 -> 3 -> 6 -> 4";
    public static String EMPTY_PARAMETER(String parameter) {
        return "Parameter: " + parameter.toUpperCase() + " is an empty or blank string";
    }
    public static String NULL_PARAMETER(String type) {
        return "Parameter: " + type.toUpperCase() + " is null";
    }
    public static String OUT_OF_BOUNDS_PARAMETER(String type) {
        return "Parameter: " + type.toUpperCase() + " length is bigger than 10";
    }
    public static String INVALID_PARAMETER_SIZE(String type) {
        return "Parameter: " + type.toUpperCase() + " length is lower than 5";
    }
    public static String NOT_ENOUGH_PARAMETERS(String type) {
        return "Not enough parameters of type " + type + " to reconstruct the secret";
    }
    public static String NOT_MATCHING_LENGTH(String parameter, int index) {
        return parameter.toUpperCase() + " number: " + index + " does not match the secret original length";
    }
    public static String NOT_MATCHING_LENGTH(String parameter) {
        return parameter.toUpperCase() + " does not match the " + parameter + " original length";
    }
    public static String INVALID_CLASSIFICATION(String parameter) {
        return "Classification provided for parameter: " + parameter.toUpperCase() + " is invalid, it must be either identifier, sensitive or quasi-identifier";
    }
    public static String NULL_METHOD(Object parameter) {
        return "Missing parameter METHOD for parameter: " + parameter.toString().toUpperCase() + " , it must be either generalization or randomization";
    }
    public static String INVALID_METHOD(Object parameter) {
        return "Method provided for parameter: " + parameter.toString().toUpperCase() + " is invalid, it must be either generalization or randomization";
    }
    public static String INVALID_KEY(Object keyIndex) {
        return "There was an error during secret reconstruction, key number: " + keyIndex.toString() + " is invalid. ";
    }
    public static String INVALID_INDEX(Object keyIndex, String type, Integer value) {
        return type.toUpperCase() + " key number: " + keyIndex + " is invalid, it must be between 1 and 10, current value is: " + value;
    }
    public static String INVALID_PARAMETER(Object parameter) {
        return "Parameter " + parameter.toString().toUpperCase() + " is invalid, it must be an object";
    }
    public static String EMPTY_PARAMETER(int index, String type) {
        return type.toUpperCase() + " key number: " + index + " is an empty or blank string";
    }
    public static String INVALID_SIZES(Object algorithm) {
        if (algorithm.toString().equalsIgnoreCase("pss")) {
            return "In order to use " + algorithm.toString().toUpperCase() + " algorithm, the parameters: SHARES, MACKEYS and MACS must have the same size";
        } else if (algorithm.toString().equalsIgnoreCase("css")) {
            return "In order to use " + algorithm.toString().toUpperCase() + " algorithm, the parameters: SHARES, FINGERPRINTS AND ENCKEYS must have the same size";
        } else {
            return "In order to use " + algorithm.toString().toUpperCase() + " algorithm, the parameters: SHARES AND ENCKEYS must have the same size";
        }
    }
    public static String INVALID_INDEXES(Object algorithm) {
        if (algorithm.toString().equalsIgnoreCase("pss")) {
            return "In order to use " + algorithm.toString().toUpperCase() + " algorithm, the parameters: SHARES, MACKEYS and MACS must have equal indexes";
        } else if (algorithm.toString().equalsIgnoreCase("pss")) {
            return "In order to use " + algorithm.toString().toUpperCase() + " algorithm, the parameters: SHARES, FINGERPRINTS AND ENCKEYS must have equal indexes";
        } else {
            return "In order to use " + algorithm.toString().toUpperCase() + " algorithm, the parameters: SHARES AND ENCKEYS must have equal indexes";
        }
    }
}
