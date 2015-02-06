package ai.api.test;

/**
 * ********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2014 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 *
 * **********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * *********************************************************************************************************************
 */

import android.text.TextUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ProtocolDevTest extends ProtocolTestBase {

    // Testing keys
    protected static final String ACCESS_TOKEN = "9586504322be4f8ba31cfdebc40eb76f";
    protected static final String SUBSCRIPTION_KEY = "cb9693af-85ce-4fbf-844a-5563722fc27f";

    @Override
    protected String getAccessToken() {
        return ACCESS_TOKEN;
    }

    @Override
    protected String getSecondAccessToken() {
        return "e807a6a95b15400a8ad18de3c577955e";
    }

    @Override
    protected String getSubscriptionKey() {
        return SUBSCRIPTION_KEY;
    }

    @Override
    protected String getRuAccessToken(){
        return "43a7541fb0a94fae8f1bef406a2d9ca8";
    }

    @Override
    protected String getBrAccessToken(){
        return "";
    }

    @Override
    protected String getPtBrAccessToken(){
        return "521282797a864a029e1f965fa973cf61";
    }

    @Override
    protected String getJaAccessToken() {
        return "c82b0a650c9a4758984fb53411f271e4";
    }

    @Override
    protected boolean isExperimentalTest() {
        return true;
    }

    @Test
    public void AIDataServiceDevRuTest() {
        if (isExperimentalTest()) {
            final AIConfiguration config = new AIConfiguration("43a7541fb0a94fae8f1bef406a2d9ca8", getSubscriptionKey(),
                    AIConfiguration.SupportedLanguages.Russian,
                    AIConfiguration.RecognitionEngine.System);

            config.setWriteSoundLog(false);
            config.setExperimental(isExperimentalTest());

            final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery("привет");

            try {
                final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

                assertFalse(TextUtils.isEmpty(aiResponse.getResult().getResolvedQuery()));
                assertEquals("helloAction", aiResponse.getResult().getAction());
                assertEquals("Добрый день", aiResponse.getResult().getSpeech());

            } catch (final AIServiceException e) {
                e.printStackTrace();
                assertTrue(e.getMessage(), false);
            }
        } else {
            assertTrue(true);
        }
    }

    @Test
    public void AIDataServiceDevTest() {
        if (isExperimentalTest()) {
            final AIConfiguration config = new AIConfiguration(getAccessToken(), getSubscriptionKey(),
                    AIConfiguration.SupportedLanguages.English,
                    AIConfiguration.RecognitionEngine.System);

            config.setWriteSoundLog(false);
            config.setExperimental(isExperimentalTest());

            final AIDataService aiDataService = new AIDataService(Robolectric.application, config);

            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery("hello");

            try {
                final AIResponse aiResponse = makeRequest(aiDataService, aiRequest);

                assertFalse(TextUtils.isEmpty(aiResponse.getResult().getResolvedQuery()));
                assertEquals("greeting", aiResponse.getResult().getAction());
                assertEquals("Hi! How are you?", aiResponse.getResult().getSpeech());

            } catch (final AIServiceException e) {
                e.printStackTrace();
                assertTrue(e.getMessage(), false);
            }
        } else {
            assertTrue(true);
        }
    }
}
