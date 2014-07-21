package com.speaktoit.ai;

import com.speaktoit.ai.model.AIError;
import com.speaktoit.ai.model.AIResponse;

public interface AIListener {
    void onResult(AIResponse result);
    void onError(AIError error);
    void onAudioLevel(float level);
    void onListeningStarted();
    void onListeningFinished();
}
