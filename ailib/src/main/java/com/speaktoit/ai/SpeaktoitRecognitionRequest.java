package com.speaktoit.ai;

import com.speaktoit.ai.model.QuestionMetadata;

public class SpeaktoitRecognitionRequest {

    private byte[] soundData;
    private QuestionMetadata metadata;

    public byte[] getSoundData() {
        return soundData;
    }

    public void setSoundData(final byte[] soundData) {
        this.soundData = soundData;
    }

    public QuestionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(final QuestionMetadata metadata) {
        this.metadata = metadata;
    }
}
