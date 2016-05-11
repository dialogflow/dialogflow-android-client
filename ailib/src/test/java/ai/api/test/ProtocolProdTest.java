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

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import ai.api.BuildConfig;

@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.TESTS_SDK)
@RunWith(RobolectricTestRunner.class)
public class ProtocolProdTest extends ProtocolTestBase {

    // Testing keys
    protected static final String ACCESS_TOKEN = "3485a96fb27744db83e78b8c4bc9e7b7";

    protected String getAccessToken() {
        return ACCESS_TOKEN;
    }

    @Override
    protected String getSecondAccessToken() {
        return "968235e8e4954cf0bb0dc07736725ecd";
    }

    protected String getRuAccessToken(){
        return "07806228a357411d83064309a279c7fd";
    }

    protected String getBrAccessToken(){
        // TODO
        return "";
    }

    protected String getPtBrAccessToken(){
        return "42db6ad6a51c47088318a8104833b66c";
    }

    @Override
    protected String getJaAccessToken() {
        // TODO
        return "";
    }

}
