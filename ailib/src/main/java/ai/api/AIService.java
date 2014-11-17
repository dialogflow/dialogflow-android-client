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

import android.content.Context;

import java.util.List;

import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

/**
 * Main SDK class fro working with API.AI service.
 */
public abstract class AIService {

    protected final AIConfiguration config;
    protected final Context context;

    protected final AIDataService aiDataService;

    private AIListener listener;

    /**
     * Use this method to get ready to work instance
     * @param context
     * @param config
     * @return instance of AIService implementation
     */
    public static AIService getService(final Context context, final AIConfiguration config) {
        if (config.getRecognitionEngine() == AIConfiguration.RecognitionEngine.Google) {
            return new GoogleRecognitionServiceImpl(context, config);
        }
        else if (config.getRecognitionEngine() == AIConfiguration.RecognitionEngine.Speaktoit) {
            return new SpeaktoitRecognitionServiceImpl(context, config);
        } else {
            throw new UnsupportedOperationException("This engine still not supported");
        }
    }

    protected AIService(final AIConfiguration config, final Context context) {
        this.config = config;
        this.context = context;

        aiDataService = new AIDataService(context, config);
    }

    /**
     * Starts listening process
     */
    public abstract void startListening();

    /**
     * Starts listening process. Request to the AI service will be done with specified contexts.
     */
    public abstract void startListening(List<AIContext> contexts);

    /**
     * Stop listening and start request to the AI service with current recognition results
     */
    public abstract void stopListening();

    /**
     * Cancel listening process and don't request to AI service
     */
    public abstract void cancel();

    /**
     * Sets listener, which used to notify about process steps
     * @param listener {@link AIListener AIListener} implementation
     */
    public void setListener(final AIListener listener) {
        this.listener = listener;
    }

    protected void onResult(final AIResponse response) {
        if (listener != null) {
            listener.onResult(response);
        }
    }

    protected void onError(final AIError error) {
        if (listener != null) {
            listener.onError(error);
        }
    }

    protected void onAudioLevelChanged(final float audioLevel) {
        if (listener != null) {
            listener.onAudioLevel(audioLevel);
        }
    }

    protected void onListeningStarted() {
        if (listener != null) {
            listener.onListeningStarted();
        }
    }

    protected void onListeningFinished() {
        if (listener != null) {
            listener.onListeningFinished();
        }
    }

    public void pause() {

    }

    public void resume(){

    }

    public AIResponse textRequest(final AIRequest request) throws AIServiceException {
        return aiDataService.request(request);
    }
}
