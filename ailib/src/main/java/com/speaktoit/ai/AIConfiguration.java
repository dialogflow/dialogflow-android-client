package com.speaktoit.ai;

public class AIConfiguration {

    private static final String SERVICE_PROD_URL="https://api.api.ai/v1/";
    private static final String SERVICE_DEV_URL = "https://dev.api.ai/api/";

    protected static String PROTOCOL_VERSION = "v1";

    protected static final String QUESTION_ENDPOINT = "query/";

    private String serviceUrl;

    public String getAgentId() {
        return "Assistant";
    }

    public enum RecognitionEngine {
        Google, Speaktoit
    }

    private String accessToken;
    private String language;
    private RecognitionEngine recognitionEngine;
    private boolean debug;

    public AIConfiguration(final String accessToken, final String language) {
        this.accessToken = accessToken;
        this.language = language;
    }

    public AIConfiguration(final String accessToken, final String language, final RecognitionEngine recognitionEngine) {
        this.accessToken = accessToken;
        this.language = language;
        this.recognitionEngine = recognitionEngine;

        serviceUrl = SERVICE_PROD_URL;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public RecognitionEngine getRecognitionEngine() {
        return recognitionEngine;
    }

    public void setRecognitionEngine(final RecognitionEngine recognitionEngine) {
        this.recognitionEngine = recognitionEngine;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;

        if (debug) {
            serviceUrl = SERVICE_DEV_URL;
        } else {
            serviceUrl = SERVICE_PROD_URL;
        }
    }

    public String getQuestionUrl() {
        return serviceUrl + QUESTION_ENDPOINT;
    }
}
