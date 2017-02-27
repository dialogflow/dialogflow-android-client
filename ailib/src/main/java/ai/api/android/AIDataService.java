package ai.api.android;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 *
 * *********************************************************************************************************************
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
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.TimeZone;

import ai.api.AIServiceContext;

/**
 * Do simple requests to the AI Service
 */
public class AIDataService extends ai.api.AIDataService {


    public static final String TAG = AIDataService.class.getName();

    @NonNull
    private final Context context;

    @NonNull
    private final AIConfiguration config;

    @NonNull
    private final Gson gson = GsonFactory.getGson();

    public AIDataService(@NonNull final Context context, @NonNull final AIConfiguration config) {
        super(config, new AIAndroidServiceContext(context));
        this.context = context;
        this.config = config;
    }

    private static class AIAndroidServiceContext implements AIServiceContext {

        private final String sessionId;

        public AIAndroidServiceContext(final Context context) {
            sessionId = SessionIdStorage.getSessionId(context);
        }

        public String getSessionId() {
            return sessionId;
        }

        public TimeZone getTimeZone() { return TimeZone.getDefault(); }
    }
}
