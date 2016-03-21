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
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class VoiceActivityDetector {

    public static final String TAG = VoiceActivityDetector.class.getName();

    public static final int FRAME_SIZE_IN_BYTES = 320;
    private static final int SEQUENCE_LENGTH_MILLIS = 30;
    private static final int MIN_SPEECH_SEQUENCE_COUNT = 3;
    private static final long MIN_SILENCE_MILLIS = 800;
    private static final long MAX_SILENCE_MILLIS = 3500;
    private static final long SILENCE_DIFF_MILLIS = MAX_SILENCE_MILLIS - MIN_SILENCE_MILLIS;
    private static final int NOISE_FRAMES = 15;
    public static final int NOISE_BYTES = NOISE_FRAMES * FRAME_SIZE_IN_BYTES;
    private static final double ENERGY_FACTOR = 3.1;
    private static final int MIN_CZ = 5;
    private static final int MAX_CZ = 15;

    private final int sampleRate;

    private SpeechEventsListener eventsListener;

    private double noiseEnergy = 0.0;

    private long lastActiveTime = -1;

    /**
     * last time active frame hit sequence.
     */
    private long lastSequenceTime = 0;

    /**
     * number of active frame in sequence.
     */
    private int sequenceCounter = 0;

    /**
     * current processed time in millis
     */
    private long time = 0;

    private int frameNumber;

    private long silenceMillis = MAX_SILENCE_MILLIS;

    private boolean speechActive = false;
    private boolean enabled = true;
    private boolean process = true;

    private double sum = 0;
    private int size = 0;

    public VoiceActivityDetector(final int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void processBuffer(final byte[] buffer, final int bytesRead) {
        if (!process) {
            return;
        }

        final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead).order(ByteOrder.LITTLE_ENDIAN);
        final ShortBuffer shorts = byteBuffer.asShortBuffer();

        final boolean active = isFrameActive(shorts);

        final int frameSize = bytesRead / 2; // 16 bit encoding
        time = frameNumber * frameSize * 1000 / sampleRate;

        if (active) {
            if (lastActiveTime >= 0 && (time - lastActiveTime) < SEQUENCE_LENGTH_MILLIS) {
                if (++sequenceCounter >= MIN_SPEECH_SEQUENCE_COUNT) {
                    if (!speechActive) {
                        onSpeechBegin();
                    }

                    lastSequenceTime = time;
                    silenceMillis = Math.max(MIN_SILENCE_MILLIS, silenceMillis - SILENCE_DIFF_MILLIS / 4);
                }
            } else {
                sequenceCounter = 1;
            }
            lastActiveTime = time;
        } else {
            if (time - lastSequenceTime > silenceMillis) {
                if (speechActive) {
                    onSpeechEnd();
                } else {
                    onSpeechCancel();
                }
            }
        }
    }

    private boolean isFrameActive(final ShortBuffer frame) {

        int lastSign = 0;
        int czCount = 0;
        double energy = 0.0;

        final int frameSize = frame.limit();
        size += frameSize;

        for (int i = 0; i < frameSize; i++) {
            final short raw = frame.get(i);
            final double amplitude = (double) raw / (double) Short.MAX_VALUE;
            energy += (float) amplitude * (float) amplitude / (double) frameSize;

            sum += raw * raw;

            final int sign = (float) amplitude > 0 ? 1 : -1;
            if (lastSign != 0 && sign != lastSign) {
                czCount++;
            }
            lastSign = sign;
        }

        boolean result = false;
        if (++frameNumber < NOISE_FRAMES) {
            noiseEnergy += (energy / (double) NOISE_FRAMES);
        } else {
            if (czCount >= MIN_CZ && czCount <= MAX_CZ) {
                if (energy > noiseEnergy * ENERGY_FACTOR) {
                    result = true;
                }
            }
        }

        return result;
    }

    public double calculateRms() {
        final double rms = Math.sqrt(sum / size) / 100;
        sum = 0;
        size = 0;
        return rms;
    }

    public void reset() {
        time = 0;
        frameNumber = 0;

        noiseEnergy = 0.0;
        lastActiveTime = -1;
        lastSequenceTime = 0;
        sequenceCounter = 0;
        silenceMillis = MAX_SILENCE_MILLIS;

        speechActive = false;
        process = true;
    }

    public void setSpeechListener(final SpeechEventsListener eventsListener) {
        this.eventsListener = eventsListener;
    }

    private void onSpeechEnd() {
        Log.v(TAG, "onSpeechEnd");

        speechActive = false;
        process = false;

        if (enabled) {
            if (eventsListener != null) {
                eventsListener.onSpeechEnd();
            }
        }
    }

    private void onSpeechCancel() {
        Log.v(TAG, "onSpeechCancel");

        speechActive = false;
        process = false;

        if (eventsListener != null) {
            eventsListener.onSpeechCancel();
        }
    }

    private void onSpeechBegin() {
        Log.v(TAG, "onSpeechBegin");

        speechActive = true;

        if (eventsListener != null) {
            eventsListener.onSpeechBegin();
        }
    }

    /**
     * If enabled, voice activity detector fires onSpeechEnd events.
     * This option does not affect onSpeechBegin and onChangeLevel events
     *
     * @param enabled new option values
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Used to notify about speech begin/end events
     */
    public interface SpeechEventsListener {
        void onSpeechBegin();

        void onSpeechCancel();

        void onSpeechEnd();
    }
}
