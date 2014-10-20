package ai.api.test;

import android.test.mock.MockContext;
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
            final AIResponse aiResponse = aiDataService.voiceRequest(inputStream);
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

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        }
    }

    private AIResponse makeRequest(final AIDataService aiDataService, final AIRequest aiRequest) throws AIServiceException {
        final AIResponse aiResponse = aiDataService.request(aiRequest);
        assertNotNull(aiResponse);
        assertFalse(aiResponse.isError());
        assertFalse(TextUtils.isEmpty(aiResponse.getId()));
        assertNotNull(aiResponse.getResult());
        return aiResponse;
    }
}
