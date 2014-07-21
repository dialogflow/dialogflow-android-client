package com.speaktoit.ai.test;

import android.text.TextUtils;

import com.speaktoit.ai.AIConfiguration;
import com.speaktoit.ai.AIDataService;
import com.speaktoit.ai.AIServiceException;
import com.speaktoit.ai.model.AIRequest;
import com.speaktoit.ai.model.AIResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ProtocolTest {

    private static final String ACCESS_TOKEN = "e43c0g5d787787d95221c9481cw8fe98";

    @Test
    public void testCheck() {
        assertTrue(true);
    }

    @Test
    public void AIDataServiceDebugTest() {
        final AIConfiguration config = new AIConfiguration(ACCESS_TOKEN, Locale.US.toString(), AIConfiguration.RecognitionEngine.Google);
        config.setDebug(true);

        final AIDataService aiDataService = new AIDataService(config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("Hello");

        try {
            final AIResponse aiResponse = aiDataService.request(aiRequest);
            assertNotNull(aiResponse);
            assertFalse(aiResponse.isError());
            assertFalse(TextUtils.isEmpty(aiResponse.getId()));
            assertNotNull(aiResponse.getResult());

            assertFalse(TextUtils.isEmpty(aiResponse.getResult().getAction()));

        } catch (final AIServiceException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
