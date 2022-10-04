package com.ifsc.secstor.api.advice.paths;

public class Paths {

    public static final String SECRET_SHARING_BASE = "/api/v1/secret-sharing";
    public static final String SECRET_SHARING_SPLIT = "/split";
    public static final String SECRET_SHARING_BASE_AND_SPLIT = SECRET_SHARING_BASE + SECRET_SHARING_SPLIT;
    public static final String SECRET_SHARING_RECONSTRUCT = "/reconstruct";
    public static final String SECRET_SHARING_BASE_AND_RECONSTRUCT = SECRET_SHARING_BASE + SECRET_SHARING_RECONSTRUCT;
    public static final String DATA_ANONYMIZATION_BASE = "/api/v1/data-anonymization";
    public static final String DATA_ANONYMIZATION_ANONYMIZE = "/anonymize";
    public static final String DATA_ANONYMIZATION_BASE_AND_ANONYMIZE = DATA_ANONYMIZATION_BASE + DATA_ANONYMIZATION_ANONYMIZE;

}
