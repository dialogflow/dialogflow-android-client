package ai.api.test.compatibility;

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

import android.content.Context;

import java.io.InputStream;
import java.net.MalformedURLException;

import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.AIServiceException;

public class SimpleProtocolTestingService extends AIDataService {
    public SimpleProtocolTestingService(final Context context, final AIConfiguration config) {
        super(context, config);
    }

    public String doDefaultProtocolTextRequest(final String requestJson) throws MalformedURLException, AIServiceException {
        return doTextRequest(requestJson);
    }

    public String doDefaultProtocolSoundRequest(final InputStream voiceStream, final String queryData) throws MalformedURLException, AIServiceException {
        return doSoundRequest(voiceStream, queryData);
    }
}
