package ai.api.test;

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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStream;

import ai.api.BuildConfig;
import ai.api.util.VoiceActivityDetector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.TESTS_SDK)
@RunWith(RobolectricTestRunner.class)
public class VADTest {

    private static final int SAMPLE_RATE = 16000;

    boolean voiceDetected = false;
    boolean speechStarted = false;

    @Test
    public void testSpeechDetect() {
        final VoiceActivityDetector vad = new VoiceActivityDetector(SAMPLE_RATE);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("speech.raw");

        voiceDetected = false;
        speechStarted = false;

        vad.setSpeechListener(new VoiceActivityDetector.SpeechEventsListener() {
            @Override
            public void onSpeechBegin() {
                speechStarted = true;
            }

            @Override
            public void onSpeechCancel() {

            }

            @Override
            public void onSpeechEnd() {
                voiceDetected = true;
            }
        });

        try {
            final byte[] frame = new byte[VoiceActivityDetector.FRAME_SIZE_IN_BYTES];
            while (inputStream.read(frame, 0, frame.length) == frame.length) {
                vad.processBuffer(frame, frame.length);
            }

            assertTrue(speechStarted);
            assertTrue(voiceDetected);

        } catch (final Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void testSilence() {
        final VoiceActivityDetector vad = new VoiceActivityDetector(SAMPLE_RATE);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("silence.raw");

        voiceDetected = false;

        vad.setSpeechListener(new VoiceActivityDetector.SpeechEventsListener() {
            @Override
            public void onSpeechBegin() {
            }

            @Override
            public void onSpeechCancel() {
            }

            @Override
            public void onSpeechEnd() {
                voiceDetected = true;
            }

        });

        try {
            final byte[] frame = new byte[VoiceActivityDetector.FRAME_SIZE_IN_BYTES];
            while (inputStream.read(frame, 0, frame.length) == frame.length) {
                vad.processBuffer(frame, frame.length);
            }

            assertFalse(voiceDetected);

        } catch (final Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    // @Test TODO enable after VAD improvement
    public void testNoise() {
        final VoiceActivityDetector vad = new VoiceActivityDetector(SAMPLE_RATE);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("noiseOnly.raw");

        voiceDetected = false;

        vad.setSpeechListener(new VoiceActivityDetector.SpeechEventsListener() {
            @Override
            public void onSpeechBegin() {

            }

            @Override
            public void onSpeechCancel() {

            }

            @Override
            public void onSpeechEnd() {
                voiceDetected = true;
            }

        });

        try {
            final byte[] frame = new byte[VoiceActivityDetector.FRAME_SIZE_IN_BYTES];
            while (inputStream.read(frame, 0, frame.length) == frame.length) {
                vad.processBuffer(frame, frame.length);
            }

            assertFalse(voiceDetected);

        } catch (final Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void testEnabled() {
        final VoiceActivityDetector vad = new VoiceActivityDetector(SAMPLE_RATE);
        vad.setEnabled(false);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("speech.raw");

        voiceDetected = false;
        speechStarted = false;

        vad.setSpeechListener(new VoiceActivityDetector.SpeechEventsListener() {
            @Override
            public void onSpeechBegin() {
                speechStarted = true;
            }

            @Override
            public void onSpeechCancel() {

            }

            @Override
            public void onSpeechEnd() {
                voiceDetected = true;
            }

        });

        try {
            final byte[] frame = new byte[VoiceActivityDetector.FRAME_SIZE_IN_BYTES];
            while (inputStream.read(frame, 0, frame.length) == frame.length) {
                vad.processBuffer(frame, frame.length);
            }

            assertTrue(speechStarted);
            assertFalse(voiceDetected);

        } catch (final Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

}
