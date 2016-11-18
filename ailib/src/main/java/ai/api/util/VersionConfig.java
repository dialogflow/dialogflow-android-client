package ai.api.util;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2016 by Speaktoit, Inc. (https://www.speaktoit.com)
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
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import ai.api.android.GsonFactory;

public class VersionConfig {

    private static final String TAG = VersionConfig.class.getName();
    private static final Pattern DOT_PATTERN = Pattern.compile(".", Pattern.LITERAL);

    private static final Map<String, VersionConfig> configuration = new HashMap<>();

    static {
        configuration.put("5.9.26", new VersionConfig(true, true));
        configuration.put("4.7.13", new VersionConfig(false, false));
    }

    private boolean destroyRecognizer = true;
    private boolean autoStopRecognizer = false;

    private VersionConfig() {
    }

    private VersionConfig(final boolean destroyRecognizer, final boolean autoStopRecognizer) {
        this.destroyRecognizer = destroyRecognizer;
        this.autoStopRecognizer = autoStopRecognizer;
    }

    public static VersionConfig init(final Context context) {
        return getConfigByVersion(context);
    }

    private static VersionConfig getConfigByVersion(final Context context) {
        final long number = numberFromBuildVersion(RecognizerChecker.getGoogleRecognizerVersion(context));

        final VersionConfig config = new VersionConfig();
        long prevVersionNumber = 0;

        for (final Map.Entry<String, VersionConfig> configEntry : configuration.entrySet()) {
            final String versionName = configEntry.getKey();

            if (!TextUtils.isEmpty(versionName)) {
                final long versionNumber = numberFromBuildVersion(versionName);
                if (number >= versionNumber && prevVersionNumber < versionNumber) {
                    config.destroyRecognizer = configEntry.getValue().destroyRecognizer;
                    config.autoStopRecognizer = configEntry.getValue().autoStopRecognizer;
                    prevVersionNumber = versionNumber;
                }
            }
        }

        return config;
    }

    public boolean isDestroyRecognizer() {
        return destroyRecognizer;
    }

    public boolean isAutoStopRecognizer() {
        return autoStopRecognizer;
    }

    private static long numberFromBuildVersion(final String buildVersion) {
        if (TextUtils.isEmpty(buildVersion))
            return 0;

        final String[] parts = DOT_PATTERN.split(buildVersion);

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Math.min(3, parts.length); i++) {
            builder.append(parts[i]);
        }
        try {
            return Long.parseLong(builder.toString());
        } catch (final NumberFormatException ignored) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return GsonFactory.getGson().toJson(this);
    }
}