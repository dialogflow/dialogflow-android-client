package ai.api;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 *
 * *********************************************************************************************************************
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

import android.content.res.AssetFileDescriptor;
import android.text.TextUtils;

import java.net.Proxy;

public class AIConfiguration {

    private static final String SERVICE_PROD_URL = "https://api.api.ai/v1/";

    protected static final String CURRENT_PROTOCOL_VERSION = "20150910";

    protected static final String QUESTION_ENDPOINT = "query";
    protected static final String USER_ENTITIES_ENDPOINT = "userEntities";

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
        English("en"),
        EnglishUS("en-US", "en"),
        EnglishGB("en-GB", "en"),
        Russian("ru"),
        German("de"),
        Portuguese("pt"),
        PortugueseBrazil("pt-BR"),
        Spanish("es"),
        French("fr"),
        Italian("it"),
        Japanese("ja"),
        Korean("ko"),
        ChineseChina("zh-CN"),
        ChineseHongKong("zh-HK"),
        ChineseTaiwan("zh-TW");

        private final String languageTag;
        private final String apiaiLanguage;

        SupportedLanguages(final String languageTag) {
            this.languageTag = languageTag;
            this.apiaiLanguage = languageTag;
        }

        SupportedLanguages(final String languageTag, final String apiaiLanguage) {
            this.languageTag = languageTag;
            this.apiaiLanguage = apiaiLanguage;
        }

        public static SupportedLanguages fromLanguageTag(final String languageTag) {
            switch (languageTag) {
                case "en":
                    return English;
                case "en-US":
                    return EnglishUS;
                case "en-GB":
                    return EnglishGB;
                case "ru":
                    return Russian;
                case "de":
                    return German;
                case "pt":
                    return Portuguese;
                case "pt-BR":
                    return PortugueseBrazil;
                case "es":
                    return Spanish;
                case "fr":
                    return French;
                case "it":
                    return Italian;
                case "ja":
                    return Japanese;
                case "ko":
                    return Korean;
                case "zh-CN":
                    return ChineseChina;
                case "zh-HK":
                    return ChineseHongKong;
                case "zh-TW":
                    return ChineseTaiwan;
                default:
                    return English;
            }
        }
    }

    private final String apiKey;
    private final SupportedLanguages language;
    private final RecognitionEngine recognitionEngine;

    /**
     * Speaktoit recognition start sound resouce descriptor
     */
    private AssetFileDescriptor recognizerStartSound;

    /**
     * Speaktoit recognition stop sound resouce descriptor
     */
    private AssetFileDescriptor recognizerStopSound;

    /**
     * Speaktoit recognition cancel sound resource descriptor
     */
    private AssetFileDescriptor recognizerCancelSound;

    /**
     * Protocol version used for api queries. Can be changed if old protocol version required.
     */
    private String protocolVersion;

    private boolean writeSoundLog = false;

    private boolean voiceActivityDetectionEnabled = true;

    private boolean normalizeInputSound = false;

    private Proxy proxy;

    public AIConfiguration(final String clientAccessToken, final SupportedLanguages language, final RecognitionEngine recognitionEngine) {
        this.apiKey = clientAccessToken;
        this.language = language;
        this.recognitionEngine = recognitionEngine;

        protocolVersion = CURRENT_PROTOCOL_VERSION;

        if (recognitionEngine == RecognitionEngine.Speaktoit
                && language == SupportedLanguages.Korean) {
            throw new UnsupportedOperationException("Only System recognition supported for Korean language");
        }

        serviceUrl = SERVICE_PROD_URL;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getLanguage() {
        return language.languageTag;
    }

    public String getApiAiLanguage() {
        return language.apiaiLanguage;
    }

    public RecognitionEngine getRecognitionEngine() {
        return recognitionEngine;
    }

    public boolean isVoiceActivityDetectionEnabled() {
        return voiceActivityDetectionEnabled;
    }

    /**
     * With setting this field to false you can disable voice activity detection for Speaktoit recognition.
     * This option does not affect System recognition.
     * @param voiceActivityDetectionEnabled
     */
    public void setVoiceActivityDetectionEnabled(final boolean voiceActivityDetectionEnabled) {
        this.voiceActivityDetectionEnabled = voiceActivityDetectionEnabled;
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

    /**
     * Check list of supported protocol versions on the api.ai website.
     * @return protocol version in YYYYMMDD format
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Set protocol version for API queries. Must be in YYYYMMDD format.
     * This option for special cases only, should not be used in usual cases.
     * @param protocolVersion Protocol version in YYYYMMDD format or empty string for the oldest version.
     *                        Check list of supported protocol versions on the api.ai website.
     */
    public void setProtocolVersion(final String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Set API service url. Used primarily for test requests.
     */
    public void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * With setting this field to true you can enable sound amplification if it's too quiet. This option improves recognition quality on some devices.
     * This option does not affect System recognition.
     * @param normalizeInputSound
     */
    public void setNormalizeInputSound(final boolean normalizeInputSound) {
        this.normalizeInputSound = normalizeInputSound;
    }

    public boolean isNormalizeInputSound() {
        return normalizeInputSound;
    }

    public String getQuestionUrl() {
        if (!TextUtils.isEmpty(protocolVersion)) {
            return String.format("%s%s?v=%s", serviceUrl, QUESTION_ENDPOINT, protocolVersion);
        } else {
            return String.format("%s%s", serviceUrl, QUESTION_ENDPOINT);
        }
    }

    String getUserEntitiesEndpoint(final String sessionId) {
        if (!TextUtils.isEmpty(protocolVersion)) {
            return String.format("%s%s?v=%s&sessionId=%s", serviceUrl, USER_ENTITIES_ENDPOINT, protocolVersion, sessionId);
        } else {
            return String.format("%s%s?sessionId=%s", serviceUrl, USER_ENTITIES_ENDPOINT, sessionId);
        }
    }

    public AssetFileDescriptor getRecognizerStartSound() {
        return recognizerStartSound;
    }

    public void setRecognizerStartSound(final AssetFileDescriptor recognizerStartSound) {
        this.recognizerStartSound = recognizerStartSound;
    }

    public AssetFileDescriptor getRecognizerStopSound() {
        return recognizerStopSound;
    }

    public void setRecognizerStopSound(final AssetFileDescriptor recognizerStopSound) {
        this.recognizerStopSound = recognizerStopSound;
    }

    public AssetFileDescriptor getRecognizerCancelSound() {
        return recognizerCancelSound;
    }

    public void setRecognizerCancelSound(final AssetFileDescriptor recognizerCancelSound) {
        this.recognizerCancelSound = recognizerCancelSound;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(final Proxy proxy) {
        this.proxy = proxy;
    }
}
