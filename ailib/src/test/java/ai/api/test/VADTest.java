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

import ai.api.util.VoiceActivityDetector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class VADTest {

    private static final int SAMPLE_RATE = 16000;

    boolean voiceDetected = false;

    @Test
    public void testSpeechDetect() {
        final VoiceActivityDetector voiceActivityDetector = new VoiceActivityDetector(SAMPLE_RATE);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("speech.raw");

        voiceDetected = false;

        voiceActivityDetector.setSpeechListener(new VoiceActivityDetector.SpeechEventsListener() {
            @Override
            public void onSpeechBegin() {

            }

            @Override
            public void onSpeechEnd() {
                voiceDetected = true;
            }
        });

        try {

            final int bufferSize = 1096;
            byte[] buffer = new byte[bufferSize];

            int bytesRead = 0;

            bytesRead = inputStream.read(buffer, 0, bufferSize);

            while (bytesRead >= 0) {
                voiceActivityDetector.processBuffer(buffer, bytesRead);
                bytesRead = inputStream.read(buffer, 0, bufferSize);
            }

            assertTrue(voiceDetected);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void testSilence() {
        final VoiceActivityDetector voiceActivityDetector = new VoiceActivityDetector(SAMPLE_RATE);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("silence.raw");

        voiceDetected = false;

        voiceActivityDetector.setSpeechListener(new VoiceActivityDetector.SpeechEventsListener() {
            @Override
            public void onSpeechBegin() {

            }

            @Override
            public void onSpeechEnd() {
                voiceDetected = true;
            }
        });

        try {

            final int bufferSize = 1096;
            byte[] buffer = new byte[bufferSize];

            int bytesRead = inputStream.read(buffer, 0, bufferSize);
            while (bytesRead >= 0) {
                voiceActivityDetector.processBuffer(buffer, bytesRead);
                bytesRead = inputStream.read(buffer, 0, bufferSize);
            }

            assertFalse(voiceDetected);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void testNoise() {
        final VoiceActivityDetector voiceActivityDetector = new VoiceActivityDetector(SAMPLE_RATE);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("noiseOnly.raw");

        voiceDetected = false;

        voiceActivityDetector.setSpeechListener(new VoiceActivityDetector.SpeechEventsListener() {
            @Override
            public void onSpeechBegin() {

            }

            @Override
            public void onSpeechEnd() {
                voiceDetected = true;
            }
        });

        try {

            final int bufferSize = 1096;
            byte[] buffer = new byte[bufferSize];

            int bytesRead = inputStream.read(buffer, 0, bufferSize);
            while (bytesRead >= 0) {
                voiceActivityDetector.processBuffer(buffer, bytesRead);
                bytesRead = inputStream.read(buffer, 0, bufferSize);
            }

            assertFalse(voiceDetected);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

}
