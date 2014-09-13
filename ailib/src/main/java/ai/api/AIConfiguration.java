package ai.api;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2014 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************/

public class AIConfiguration {

    private static final String SERVICE_PROD_URL="https://api.api.ai/v1-dev/";
    private static final String SERVICE_DEV_URL = "https://dev.api.ai/api/";

    protected static String PROTOCOL_VERSION = "v1";

    protected static final String QUESTION_ENDPOINT = "query";

    private String serviceUrl;

    public String getAgentId() {
        return "Assistant";
    }

    public enum RecognitionEngine {

        /**
         * Google Speech Recognition integrated into Android OS
         */
        Google,

        /**
         * Not implemented yet. Do not use.
         */
        Speaktoit
    }

    private String apiKey;
    private final String subscriptionKey;
    private String language;
    private RecognitionEngine recognitionEngine;
    private boolean debug;

    public AIConfiguration(final String apiKey, final String subscriptionKey, final String language, final RecognitionEngine recognitionEngine) {
        this.apiKey = apiKey;
        this.subscriptionKey = subscriptionKey;
        this.language = language;
        this.recognitionEngine = recognitionEngine;

        serviceUrl = SERVICE_PROD_URL;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSubscriptionKey() {
        return subscriptionKey;
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
        return String.format("%s%s", serviceUrl, QUESTION_ENDPOINT);
    }
}
