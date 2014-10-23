package ai.api.util;

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

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class VoiceActivityDetector {

    public static final String TAG = VoiceActivityDetector.class.getName();

    private final int sampleRate;

    private SpeechEventsListener eventsListener;

    private double averageNoiseEnergy = 0.0;

    private double lastActiveTime = -1.0;

    /**
     * last time active frame hit sequance.
     */
    private double lastSequenceTime = 0.0;

    /**
     * number of active frame in sequance.
     */
    private int sequenceCounter = 0;

    /**
     * current processed time in millis
     */
    private double time = 0.0;

    private final double sequenceLengthMilis = 100.0;
    private final int minSpeechSequenceCount = 3;

    /**
     * multiplayer for energy noise overcome.
     */
    private final double energyFactor = 1.1;

    private final double maxSilenceLengthMilis = 0.35 * 1000;
    private final double minSilenceLengthMilis = 0.08 * 1000;

    private double silenceLengthMilis = maxSilenceLengthMilis;

    private boolean speechActive = false;

    /**
     * Time in millis to remember nose energy
     */
    private final int startNoiseInterval = 150;
    private int minAudioBufferSize = 1920;

    public VoiceActivityDetector(final int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void processBuffer(byte[] buffer, int bytesRead) {

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
        ShortBuffer shorts = byteBuffer.asShortBuffer();

        boolean active = isFrameActive(shorts);

        int frameSize = bytesRead / 2; // 16 bit encoding
        time = time + (frameSize * 1000) / sampleRate; // because of sampleRate given for seconds

        if (active) {
            if (lastActiveTime >= 0 &&
                    time - lastActiveTime < sequenceLengthMilis) {

                sequenceCounter++;

                if (sequenceCounter >= minSpeechSequenceCount) {

                    if (!speechActive) {
                        onSpeechBegin();
                    }

                    speechActive = true;

                    Log.d(TAG, "LAST SPEECH " + time);
                    lastSequenceTime = time;
                    silenceLengthMilis = Math.max(minSilenceLengthMilis, silenceLengthMilis - (maxSilenceLengthMilis - minSilenceLengthMilis) / 4);
                    Log.d(TAG, "SM:" + silenceLengthMilis);

                }
            } else {
                sequenceCounter = 1;
            }
            lastActiveTime = time;
        } else {
            if (time - lastSequenceTime > silenceLengthMilis) {
                if (lastSequenceTime > 0) {
                    Log.d(TAG, "TERMINATE: " + time);
                    if (speechActive) {
                        speechActive = false;
                        onSpeechEnd();
                    }

                } else {
                    Log.d(TAG, "NOSPEECH: " + time);
                }
            }
        }

    }

    private boolean isFrameActive(final ShortBuffer frame) {

        int lastSign = 0;
        int czCount = 0;
        double energy = 0.0;

        final int frameSize = frame.limit();

        for (int i = 0; i < frameSize; i++) {
            final short amplitudeValue = frame.get(i);
            energy += amplitudeValue * amplitudeValue / frameSize;

            final int sign;

            if (amplitudeValue > 0) {
                sign = 1;
            } else {
                sign = -1;
            }

            if (lastSign != 0 && sign != lastSign) {
                czCount += 1;
            }
            lastSign = sign;
        }

        boolean result = false;
        if (time < startNoiseInterval) {
            averageNoiseEnergy = (averageNoiseEnergy + energy) / 2.0;
        } else {
            final int minCZ = (int) (frameSize * (1 / 3.0));
            final int maxCZ = (int) (frameSize * (3 / 4.0));

            if (czCount >= minCZ && czCount <= maxCZ) {
                if (energy > averageNoiseEnergy * energyFactor) {
                    result = true;
                }
            }
        }

        return result;

    }

    public void reset() {
        time = 0.0;

        averageNoiseEnergy = 0.0;
        lastActiveTime = -1.0;
        lastSequenceTime = 0.0;
        sequenceCounter = 0;
        silenceLengthMilis = maxSilenceLengthMilis;

        speechActive = false;
    }

    public void setSpeechListener(final SpeechEventsListener eventsListener) {
        this.eventsListener = eventsListener;
    }

    private void onSpeechEnd() {
        Log.v(TAG, "onSpeechEnd");
        if (eventsListener != null) {
            eventsListener.onSpeechEnd();
        }
    }

    private void onSpeechBegin() {
        Log.v(TAG, "onSpeechBegin");
        if (eventsListener != null) {
            eventsListener.onSpeechBegin();
        }
    }

    /**
     * Used for optimization
     * @param minAudioBufferSize
     */
    public void setMinAudioBufferSize(final int minAudioBufferSize) {
        this.minAudioBufferSize = minAudioBufferSize;
    }

    /**
     * Used to notify about speech begin/end events
     */
    public interface SpeechEventsListener {
        void onSpeechBegin();
        void onSpeechEnd();
    }
}
