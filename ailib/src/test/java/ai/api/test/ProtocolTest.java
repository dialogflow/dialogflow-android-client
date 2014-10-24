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

import android.text.TextUtils;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ProtocolTest {

    private static final String ACCESS_TOKEN = "YOUR_ACCESS_TOKEN_HERE";
    private static final String SUBSCRIPTION_KEY = "INSERT_SUBSCRIPTION_KEY_HERE";


    @Test
    public void testCheck() {
        assertTrue(true);
    }

    @Test
    public void AIDataServiceDebugTest() {
        final AIConfiguration config = new AIConfiguration(ACCESS_TOKEN, SUBSCRIPTION_KEY,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.Google);

        config.setWriteSoundLog(false);

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("Hello");

        try {
            final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

            assertFalse(TextUtils.isEmpty(aiResponse.getResult().getResolvedQuery()));

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void AIServiceSpeaktoitVoiceRequestTest() {
        final AIConfiguration config = new AIConfiguration(ACCESS_TOKEN, SUBSCRIPTION_KEY,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.Speaktoit);

        config.setWriteSoundLog(false);

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("log.raw");

        try {
            final AIResponse aiResponse = aiDataService.voiceRequest(inputStream, null);
            assertNotNull(aiResponse);
            assertFalse(aiResponse.isError());
            assertFalse(TextUtils.isEmpty(aiResponse.getId()));
            assertNotNull(aiResponse.getResult());

            assertEquals("what is your name", aiResponse.getResult().getResolvedQuery());

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void ContextTest() {
        final AIConfiguration config = new AIConfiguration(ACCESS_TOKEN, SUBSCRIPTION_KEY,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.Google);

        config.setWriteSoundLog(false);

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("Hello");

        AIResponse aiResponse;
        String action;

        try {
            cleanContexts(aiDataService);

            aiResponse = makeRequest(aiDataService, aiRequest);
            action = aiResponse.getResult().getAction();
            assertEquals("greeting", action);

            aiRequest.addContext(new AIContext("firstContext"));
            aiResponse = makeRequest(aiDataService, aiRequest);
            action = aiResponse.getResult().getAction();
            assertEquals("firstGreeting", action);
            assertArrayContains("firstContext", aiResponse.getResult().getMetadata().getInputContexts());
            //assertArrayContains("firstContext", aiResponse.getResult().getMetadata().getContexts());

            aiRequest.setResetContexts(true);
            aiRequest.setContexts(null);
            aiRequest.addContext(new AIContext("secondContext"));
            aiResponse = makeRequest(aiDataService, aiRequest);
            action = aiResponse.getResult().getAction();
            assertEquals("secondGreeting", action);
            assertArrayNotContains("firstContext", aiResponse.getResult().getMetadata().getInputContexts());
            assertArrayContains("secondContext", aiResponse.getResult().getMetadata().getInputContexts());
            //assertArrayNotContains("firstContext", aiResponse.getResult().getMetadata().getContexts());
            //assertArrayContains("secondContext", aiResponse.getResult().getMetadata().getContexts());

            cleanContexts(aiDataService);

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    /**
     * Cleanup contexts to prevent Tests correlation
     */
    private void cleanContexts(final AIDataService aiDataService) throws AIServiceException {
        final AIRequest cleanRequest = new AIRequest();
        cleanRequest.setResetContexts(true);
        final AIResponse response = aiDataService.request(cleanRequest);
        assertFalse(response.isError());
    }

    private AIResponse makeRequest(final AIDataService aiDataService, final AIRequest aiRequest) throws AIServiceException {
        final AIResponse aiResponse = aiDataService.request(aiRequest);
        assertNotNull(aiResponse);
        assertFalse(aiResponse.isError());
        assertFalse(TextUtils.isEmpty(aiResponse.getId()));
        assertNotNull(aiResponse.getResult());
        return aiResponse;
    }

    private void assertArrayContains(final String expected, final String[] array) {
        assertNotNull(array);
        boolean exist = false;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(expected)) {
                exist = true;
            }
        }
        assertTrue(exist);
    }

    private void assertArrayNotContains(final String expected, final String[] array) {
        assertNotNull(array);
        boolean exist = false;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(expected)) {
                exist = true;
            }
        }
        assertFalse(exist);
    }
}
