package com.speaktoit.ai;

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
