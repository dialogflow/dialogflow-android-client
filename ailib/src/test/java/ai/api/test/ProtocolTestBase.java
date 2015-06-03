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

import com.google.gson.JsonElement;

import org.junit.Test;
import org.robolectric.Robolectric;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIContext;
import ai.api.model.AIOutputContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Entity;
import ai.api.model.EntityEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class ProtocolTestBase {

    protected abstract String getAccessToken();

    protected abstract String getSecondAccessToken();

    protected abstract String getSubscriptionKey();

    protected abstract String getRuAccessToken();

    protected abstract String getBrAccessToken();

    protected abstract String getPtBrAccessToken();

    protected abstract String getJaAccessToken();

    protected ProtocolTestBase() {
    }

    @Test
    public void textRequestTest() {
        final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        updateConfig(config);

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("Hello");

        try {
            final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

            assertFalse(TextUtils.isEmpty(aiResponse.getResult().getResolvedQuery()));
            assertEquals("greeting", aiResponse.getResult().getAction());
            assertEquals("Hi! How are you?", aiResponse.getResult().getFulfillment().getSpeech());

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

        updateConfig(config);

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("what_is_your_name.raw");

        try {
            final AIResponse aiResponse = aiDataService.voiceRequest(inputStream);
            assertNotNull(aiResponse);
            assertFalse(aiResponse.getStatus().getErrorDetails(), aiResponse.isError());
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

        updateConfig(config);

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

        updateConfig(config);

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

        updateConfig(config);

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("what_is_your_name.raw");

        try {
            final AIResponse aiResponse = aiDataService.voiceRequest(inputStream);
            assertNotNull(aiResponse);
            assertFalse(aiResponse.getStatus().getErrorDetails(), aiResponse.isError());
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

            updateConfig(config);

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

            updateConfig(secondConfig);

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

        updateConfig(config);

        try {

            final AIDataService firstService = new AIDataService(Robolectric.application, config);
            final AIDataService secondService = new AIDataService(Robolectric.application, config);

            {
                final AIRequest weatherRequest = new AIRequest();
                weatherRequest.setQuery("weather");
                final AIResponse weatherResponse = makeRequest(firstService, weatherRequest);
                assertNotNull(weatherResponse);
            }

            {
                final AIRequest checkSecondRequest = new AIRequest();
                checkSecondRequest.setQuery("check weather");
                final AIResponse checkSecondResponse = makeRequest(secondService, checkSecondRequest);
                assertTrue(TextUtils.isEmpty(checkSecondResponse.getResult().getAction()));
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
    public void testParameters(){
        final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        updateConfig(config);

        try {
            final AIDataService aiDataService = new AIDataService(Robolectric.application, config);
            final AIResponse response = aiDataService.request(new AIRequest("what is your name"));

            assertNotNull(response.getResult().getParameters());
            assertFalse(response.getResult().getParameters().isEmpty());

            final AIOutputContext context = response.getResult().getContexts()[0];
            assertNotNull(context.getParameters());

            assertTrue(context.getParameters().containsKey("param"));
            final JsonElement contextParam = context.getParameters().get("param");
            assertEquals("blabla", contextParam.getAsString());

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

        updateConfig(config);

        try {
            final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery("привет");

            final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

            assertFalse(TextUtils.isEmpty(aiResponse.getResult().getResolvedQuery()));

            assertEquals("helloAction", aiResponse.getResult().getAction());
            assertEquals("Добрый день", aiResponse.getResult().getFulfillment().getSpeech());

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

        updateConfig(config);

        try {
            final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery("oi");

            final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

            assertFalse(TextUtils.isEmpty(aiResponse.getResult().getResolvedQuery()));

            assertEquals("helloAction", aiResponse.getResult().getAction());
            assertEquals("como você está", aiResponse.getResult().getFulfillment().getSpeech());

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

        updateConfig(config);

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("Hello");

        try {
            final AIResponse aiResponse = aiDataService.request(aiRequest);
            assertTrue("Method should produce exception", false);
        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertNotNull(e.getResponse());
            assertEquals("unauthorized", e.getResponse().getStatus().getErrorType());
            assertEquals("Authorization failed. Please check your access keys.", e.getMessage());
        }
    }

    @Test
    public void errorVoiceRequestTest() {
        final AIConfiguration config = new AIConfiguration("WRONG_ACCESS_TOKEN", getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        updateConfig(config);

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final InputStream voiceStream = getClass().getClassLoader().getResourceAsStream("what_is_your_name.raw");

        try {
            final AIResponse aiResponse = aiDataService.voiceRequest(voiceStream);
            assertTrue("Method should produce exception", false);
        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertNotNull(e.getResponse());
            assertEquals("unauthorized", e.getResponse().getStatus().getErrorType());
            assertEquals("Authorization failed. Please check your access keys.", e.getMessage());
        }
    }

    @Test
    public void resetContextsTest() {
        final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.Speaktoit);

        updateConfig(config);

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
                assertFalse(aiResponse.getStatus().getErrorDetails(), aiResponse.isError());
                assertNotContainsContext(aiResponse, "name_question");
            }

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void entitiesTest() throws AIServiceException {
        final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        updateConfig(config);

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("hi nori");

        final Entity myDwarfs = new Entity("dwarfs");
        myDwarfs.addEntry(new EntityEntry("Ori", new String[] {"ori", "Nori"}));
        myDwarfs.addEntry(new EntityEntry("bifur", new String[] {"Bofur","Bombur"}));

        final List<Entity> extraEntities = Collections.singletonList(myDwarfs);

        aiRequest.setEntities(extraEntities);

        final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

        assertFalse(TextUtils.isEmpty(aiResponse.getResult().getResolvedQuery()));
        assertEquals("say_hi", aiResponse.getResult().getAction());
        assertEquals("hi Bilbo, I am Ori", aiResponse.getResult().getFulfillment().getSpeech());
    }

    @Test
    public void wrongEntitiesTest() {
        final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        updateConfig(config);

        final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("hi Bofur");

        final Entity myDwarfs = new Entity("not_dwarfs");
        myDwarfs.addEntry(new EntityEntry("Nori", new String[] {"Nori","Ori"}));
        myDwarfs.addEntry(new EntityEntry("Bifur", new String[] {"Bofur","Bifur", "Bombur"}));

        final ArrayList<Entity> extraEntities = new ArrayList<>();
        extraEntities.add(myDwarfs);

        aiRequest.setEntities(extraEntities);

        final AIResponse aiResponse;
        try {
            aiResponse = makeRequest(aiDataService, aiRequest);
            assertTrue("Request should throws bad_request exception", false);
        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(true);
        }

    }

    protected void updateConfig(AIConfiguration config) {

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
        assertFalse(aiResponse.getStatus().getErrorDetails(), aiResponse.isError());
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
