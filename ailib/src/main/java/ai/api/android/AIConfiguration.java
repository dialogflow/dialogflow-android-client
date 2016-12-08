package ai.api.android;

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

public class AIConfiguration extends ai.api.AIConfiguration {
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
         *
         * @deprecated Use System instead
         * will be support until 01.02.2017
         */
        @Deprecated
        Speaktoit
    }

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

    private boolean voiceActivityDetectionEnabled = true;

    private boolean normalizeInputSound = false;

    public AIConfiguration(final String clientAccessToken, final SupportedLanguages language, final RecognitionEngine recognitionEngine) {
        super(clientAccessToken, language);

        this.recognitionEngine = recognitionEngine;

        if (recognitionEngine == RecognitionEngine.Speaktoit
                && language == SupportedLanguages.Korean) {
            throw new UnsupportedOperationException("Only System recognition supported for Korean language");
        }
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
}
