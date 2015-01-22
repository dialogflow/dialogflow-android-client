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

    private static final String SERVICE_PROD_URL="https://api.api.ai/v1/";
    private static final String SERVICE_DEV_URL = "https://dev.api.ai/api/";

    protected static String PROTOCOL_VERSION = "v1";

    protected static final String QUESTION_ENDPOINT = "query";

    private String serviceUrl;

    public enum RecognitionEngine {

        /**
         * Google Speech Recognition integrated into Android OS
         *
         * @deprecated Use System instead
         */
        @Deprecated
        Google,

        /**
         * Default system recognition
         */
        System,

        /**
         * Speaktoit recognition engine
         */
        Speaktoit
    }

    /**
     * Currently supported languages
     */
    public enum SupportedLanguages {
        English("en"), Russian("ru"), German("de"), Portuguese("pt"), PortugueseBrazil("pt-BR");

        private final String languageTag;

        private SupportedLanguages(final String languageTag) {
            this.languageTag = languageTag;
        }

        public static SupportedLanguages fromLanguageTag(final String languageTag) {
            switch (languageTag) {
                case "en":
                    return English;
                case "ru":
                    return Russian;
                case "de":
                    return German;
                case "pt":
                    return Portuguese;
                case "pt-BR":
                    return PortugueseBrazil;
                default:
                    return English;
            }
        }
    }

    private String apiKey;
    private final String subscriptionKey;
    private String language;
    private RecognitionEngine recognitionEngine;

    private boolean debug;
    private boolean writeSoundLog;

    public AIConfiguration(final String apiKey, final String subscriptionKey, final SupportedLanguages language, final RecognitionEngine recognitionEngine) {
        this.apiKey = apiKey;
        this.subscriptionKey = subscriptionKey;
        this.language = language.languageTag;
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

    /**
     * This flag is for testing purposes ONLY. Don't change it.
     * @return value indicating used service address
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * This flag is for testing purposes ONLY. Don't use it in your code.
     * @param debug value indicating used service address
     */
    public void setDebug(final boolean debug) {
        this.debug = debug;

        if (debug) {
            serviceUrl = SERVICE_DEV_URL;
        } else {
            serviceUrl = SERVICE_PROD_URL;
        }
    }

    /**
     * This flag is for testing purposes ONLY. Don't change it.
     * @param writeSoundLog value, indicating recorded sound will be saved in storage (if possible)
     */
    public void setWriteSoundLog(final boolean writeSoundLog) {
        this.writeSoundLog = writeSoundLog;
    }

    /**
     * This flag is for testing purposes ONLY. Don't use it in your code.
     * @return value, indicating recorded sound will be saved in storage  (if possible)
     */
    public boolean isWriteSoundLog() {
        return writeSoundLog;
    }

    public String getQuestionUrl() {
        return String.format("%s%s", serviceUrl, QUESTION_ENDPOINT);
    }
}
