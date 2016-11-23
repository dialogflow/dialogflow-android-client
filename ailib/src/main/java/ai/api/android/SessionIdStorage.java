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
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.UUID;

public abstract class SessionIdStorage {
    private static final String PREF_NAME = "APIAI_preferences";
    private static final String SESSION_ID = "sessionId";

    public synchronized static String getSessionId(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        final String sessionId = sharedPreferences.getString(SESSION_ID, "");
        if (!TextUtils.isEmpty(sessionId)) {
            return sessionId;
        } else {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            final String value = UUID.randomUUID().toString();
            editor.putString(SESSION_ID, value);
            editor.commit();
            return value;
        }

    }

    public static synchronized void resetSessionId(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SESSION_ID, "");
        editor.commit();
    }
}
