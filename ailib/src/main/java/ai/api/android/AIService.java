package ai.api.android;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
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

import android.Manifest;
import android.content.Context;

import java.util.Collection;
import java.util.List;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Entity;
import ai.api.services.GoogleRecognitionServiceImpl;
import ai.api.services.SpeaktoitRecognitionServiceImpl;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Main SDK class for working with API.AI service.
 */
public abstract class AIService {

    private static final String TAG = AIService.class.getName();

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
        if (config.getRecognitionEngine() == AIConfiguration.RecognitionEngine.System) {
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
     * Starts listening process. Request to the AI service will be done with specified extra data.
     * @param requestExtras extras can hold additional contexts and entities
     */
    public abstract void startListening(RequestExtras requestExtras);

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

    protected void onListeningCancelled() {
        if (listener != null) {
            listener.onListeningCanceled();
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

    public AIResponse textRequest(final String textRequest, final RequestExtras requestExtras) throws AIServiceException {
        final AIRequest aiRequest = new AIRequest(textRequest);
        if (requestExtras != null) {
            requestExtras.copyTo(aiRequest);
        }
        return aiDataService.request(aiRequest);
    }

    /**
     * Forget all old contexts
     * @return true if operation succeed, false otherwise
     */
    public boolean resetContexts() {
        return aiDataService.resetContexts();
    }

    /**
     * Upload user entity for using while session
     * @param userEntity entity to upload
     * @return uploading result
     * @throws AIServiceException
     */
    public AIResponse uploadUserEntity(final Entity userEntity) throws AIServiceException {
        return aiDataService.uploadUserEntity(userEntity);
    }

    /**
     * Upload user entities for using while session
     * @param userEntities collection of user entities
     * @return uploading result
     * @throws AIServiceException if request to the API.AI service failed
     */
    public AIResponse uploadUserEntities(final Collection<Entity> userEntities) throws AIServiceException {
        return aiDataService.uploadUserEntities(userEntities);
    }

    protected boolean checkPermissions() {
        boolean granted = true;
        try {
            granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } catch (final Throwable ignored) {
        }
        return granted;
    }
}
