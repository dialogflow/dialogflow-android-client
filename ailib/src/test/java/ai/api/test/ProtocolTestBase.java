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

import org.junit.Test;
import org.robolectric.Robolectric;

import java.io.InputStream;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIContext;
import ai.api.model.AIOutputContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class ProtocolTestBase {

    protected abstract String getAccessToken();

    protected abstract String getSecondAccessToken();

    protected abstract String getSubscriptionKey();

    protected abstract String getRuAccessToken();

    protected abstract String getBrAccessToken();

    protected abstract String getPtBrAccessToken();

    protected abstract String getJaAccessToken();

    /**
     * Test for experimental service version or not
     * @return true, if test experimental service version
     */
    protected abstract boolean isExperimentalTest();

    protected ProtocolTestBase() {
    }

    @Test
    public void textRequestTest() {
        final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setWriteSoundLog(false);
        config.setExperimental(isExperimentalTest());

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("Hello");

        try {
            final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

            assertFalse(TextUtils.isEmpty(aiResponse.getResult().getResolvedQuery()));
            assertEquals("greeting", aiResponse.getResult().getAction());
            assertEquals("Hi! How are you?", aiResponse.getResult().getSpeech());

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void voiceRequestTest() {
        final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.Speaktoit);

        config.setWriteSoundLog(false);
        config.setExperimental(isExperimentalTest());

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("log.raw");

        try {
            final AIResponse aiResponse = aiDataService.voiceRequest(inputStream, null);
            assertNotNull(aiResponse);
            assertFalse(aiResponse.isError());
            assertFalse(TextUtils.isEmpty(aiResponse.getId()));
            assertNotNull(aiResponse.getResult());

            final String resolvedQuery = aiResponse.getResult().getResolvedQuery();
            assertFalse(TextUtils.isEmpty(resolvedQuery));
            assertTrue(resolvedQuery.contains("what is your"));
            assertTrue(resolvedQuery.contains("name"));

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void inputContextTest() {
        final AIConfiguration config = new AIConfiguration(getAccessToken(),
                getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setWriteSoundLog(false);
        config.setExperimental(isExperimentalTest());

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

            aiRequest.setResetContexts(true);
            aiRequest.setContexts(null);
            aiRequest.addContext(new AIContext("secondContext"));
            aiResponse = makeRequest(aiDataService, aiRequest);
            action = aiResponse.getResult().getAction();
            assertEquals("secondGreeting", action);

            cleanContexts(aiDataService);

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void outputContextTest() {
        final AIConfiguration config = new AIConfiguration(getAccessToken(),
                getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setExperimental(isExperimentalTest());

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("weather");

        try {
            cleanContexts(aiDataService);

            final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);
            final String action = aiResponse.getResult().getAction();
            assertEquals("showWeather", action);
            assertNotNull(aiResponse.getResult().getContexts());

            assertContainsContext(aiResponse, "weather");

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    private void assertContainsContext(final AIResponse aiResponse, final String contextName) {
        boolean contextExist = false;
        for (final AIOutputContext outputContext : aiResponse.getResult().getContexts()) {
            if (outputContext.getName().equalsIgnoreCase(contextName)) {
                contextExist = true;
            }
        }
        assertTrue(contextExist);
    }

    private void assertNotContainsContext(final AIResponse aiResponse, final String contextName) {
        boolean contextExist = false;
        for (final AIOutputContext outputContext : aiResponse.getResult().getContexts()) {
            if (outputContext.getName().equalsIgnoreCase(contextName)) {
                contextExist = true;
            }
        }
        assertFalse(contextExist);
    }

    @Test
    public void outputContextVoiceTest() {
        final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.Speaktoit);

        config.setWriteSoundLog(false);
        config.setExperimental(isExperimentalTest());

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("log.raw");

        try {
            final AIResponse aiResponse = aiDataService.voiceRequest(inputStream, null);
            assertNotNull(aiResponse);
            assertFalse(aiResponse.isError());
            assertFalse(TextUtils.isEmpty(aiResponse.getId()));
            assertNotNull(aiResponse.getResult());

            final String resolvedQuery = aiResponse.getResult().getResolvedQuery();
            assertFalse(TextUtils.isEmpty(resolvedQuery));
            assertTrue(resolvedQuery.contains("what is your"));

            assertContainsContext(aiResponse, "name_question");

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void differentAgentsTest() {

        final String query = "I want pizza";

        {
            final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                    AIConfiguration.SupportedLanguages.English,
                    AIConfiguration.RecognitionEngine.System);

            config.setWriteSoundLog(false);
            config.setExperimental(isExperimentalTest());

            final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery(query);

            try {
                final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

                assertNotNull(aiResponse.getResult());
                assertEquals("pizza", aiResponse.getResult().getAction());

            } catch (final AIServiceException e) {
                e.printStackTrace();
                assertTrue(e.getMessage(), false);
            }
        }

        {
            final AIConfiguration secondConfig = new AIConfiguration(getSecondAccessToken(), getSubscriptionKey(),
                    AIConfiguration.SupportedLanguages.English,
                    AIConfiguration.RecognitionEngine.System);

            secondConfig.setWriteSoundLog(false);
            secondConfig.setExperimental(isExperimentalTest());

            final AIDataService aiDataService = new AIDataService(Robolectric.application, secondConfig);

            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery(query);

            try {
                final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

                assertNotNull(aiResponse.getResult());
                assertTrue(TextUtils.isEmpty(aiResponse.getResult().getAction()));

            } catch (final AIServiceException e) {
                e.printStackTrace();
                assertTrue(e.getMessage(), false);
            }
        }
    }

    @Test
    public void sessionTest() {
        final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setExperimental(isExperimentalTest());

        try {

            final AIDataService firstService = new AIDataService(Robolectric.application, config);
            final AIDataService secondService = new AIDataService(Robolectric.application, config);

            {
                final AIRequest weatherRequest = new AIRequest();
                weatherRequest.setQuery("weather");
                final AIResponse weatherResponse = makeRequest(firstService, weatherRequest);
            }

            {
                final AIRequest checkSecondRequest = new AIRequest();
                checkSecondRequest.setQuery("check weather");
                final AIResponse checkSecondResponse = makeRequest(secondService, checkSecondRequest);
                assertNull(checkSecondResponse.getResult().getAction());
            }

            {
                final AIRequest checkFirstRequest = new AIRequest();
                checkFirstRequest.setQuery("check weather");
                final AIResponse checkFirstResponse = makeRequest(firstService, checkFirstRequest);
                assertNotNull(checkFirstResponse.getResult().getAction());
                assertTrue(checkFirstResponse.getResult().getAction().equalsIgnoreCase("checked"));
            }

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }

    }

    @Test
    public void testRussianLanguage() {
        final AIConfiguration config = new AIConfiguration(getRuAccessToken(),
                getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.Russian,
                AIConfiguration.RecognitionEngine.System);

        config.setExperimental(isExperimentalTest());

        try {
            final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery("привет");

            final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

            assertFalse(TextUtils.isEmpty(aiResponse.getResult().getResolvedQuery()));

            assertEquals("helloAction", aiResponse.getResult().getAction());
            assertEquals("Добрый день", aiResponse.getResult().getSpeech());

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void testBrazilLanguage() {
        final AIConfiguration config = new AIConfiguration(getPtBrAccessToken(),
                getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.PortugueseBrazil,
                AIConfiguration.RecognitionEngine.System);

        config.setExperimental(isExperimentalTest());

        try {
            final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery("oi");

            final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

            assertFalse(TextUtils.isEmpty(aiResponse.getResult().getResolvedQuery()));

            assertEquals("helloAction", aiResponse.getResult().getAction());
            assertEquals("como você está", aiResponse.getResult().getSpeech());

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void errorTextRequestTest() {
        final AIConfiguration config = new AIConfiguration("WRONG_ACCESS_TOKEN", getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setExperimental(isExperimentalTest());

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("Hello");

        try {
            final AIResponse aiResponse = aiDataService.request(aiRequest);

            assertTrue(aiResponse.isError());
            assertNull(aiResponse.getResult());
            assertNotNull(aiResponse.getStatus().getErrorID());

            assertEquals(401, (int) aiResponse.getStatus().getCode());
            assertEquals("unauthorized", aiResponse.getStatus().getErrorType());

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }

    }

    @Test
    public void resetContextsTest() {
        final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.Speaktoit);
        config.setExperimental(isExperimentalTest());

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);
        try {

            {
                final AIRequest aiRequest = new AIRequest("what is your name");

                final AIResponse aiResponse = aiDataService.request(aiRequest);
                assertContainsContext(aiResponse, "name_question");

                final boolean resetSucceed = aiDataService.resetContexts();
                assertTrue(resetSucceed);
            }

            {
                final AIRequest aiRequest = new AIRequest("hello");
                final AIResponse aiResponse = aiDataService.request(aiRequest);
                assertNotNull(aiResponse);
                assertFalse(aiResponse.isError());
                assertNotContainsContext(aiResponse, "name_question");
            }

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }


    /**
     * Cleanup contexts to prevent Tests correlation
     */
    protected void cleanContexts(final AIDataService aiDataService) throws AIServiceException {
        aiDataService.resetContexts();
    }

    protected AIResponse makeRequest(final AIDataService aiDataService, final AIRequest aiRequest) throws AIServiceException {
        final AIResponse aiResponse = aiDataService.request(aiRequest);
        assertNotNull(aiResponse);
        assertFalse(aiResponse.isError());
        assertFalse(TextUtils.isEmpty(aiResponse.getId()));
        assertNotNull(aiResponse.getResult());
        return aiResponse;
    }

    protected void assertArrayContains(final String expected, final String[] array) {
        assertNotNull(array);
        boolean exist = false;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(expected)) {
                exist = true;
            }
        }
        assertTrue(exist);
    }

    protected void assertArrayNotContains(final String expected, final String[] array) {
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
