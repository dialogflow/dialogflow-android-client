package com.speaktoit.ai;

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

import com.speaktoit.ai.model.AIError;
import com.speaktoit.ai.model.AIResponse;

public abstract class AIService {

    protected final AIConfiguration config;
    private AIListener listener;

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

    protected AIService(final AIConfiguration config) {
        this.config = config;
    }

    public abstract void startListening();

    public abstract void stopListening();

    public abstract void cancel();

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
}
