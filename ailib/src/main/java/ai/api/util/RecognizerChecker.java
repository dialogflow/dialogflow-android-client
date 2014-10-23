package ai.api.util;

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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.speech.RecognitionService;

import java.util.LinkedList;
import java.util.List;

public class RecognizerChecker {

    public static final String GOOGLE_RECOGNIZER_PACKAGE_NAME = "com.google.android.googlequicksearchbox";
    public static final String GOOGLE_VOICE_SEARCH_PACKAGE_NAME = "com.google.android.voicesearch";

    public static ComponentName findGoogleRecognizer(final Context context) {
        return findRecognizerByPackage(context, getPackageName());
    }

    private static ComponentName findRecognizerByPackage(final Context context, final String prefPackage) {
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> available = pm != null ? pm.queryIntentServices(new Intent(RecognitionService.SERVICE_INTERFACE), 0) : new LinkedList<ResolveInfo>();
        final int numAvailable = available.size();

        if (numAvailable == 0) {
            // no available voice recognition services found
            return null;
        } else {
            if (prefPackage != null) {
                for (final ResolveInfo anAvailable : available) {
                    final ServiceInfo serviceInfo = anAvailable.serviceInfo;

                    if (serviceInfo != null && prefPackage.equals(serviceInfo.packageName)) {
                        return new ComponentName(serviceInfo.packageName, serviceInfo.name);
                    }
                }
            }
            // Do not pick up first available, but use default one
            return null;
        }
    }

    public static String getGoogleRecognizerVersion(final Context context) {
        try {
            final PackageManager pm = context.getPackageManager();

            final ComponentName recognizerComponentName = findGoogleRecognizer(context);
            if (recognizerComponentName != null) {
                final PackageInfo packageInfo = pm.getPackageInfo(recognizerComponentName.getPackageName(), 0);
                final String versionName = packageInfo.versionName;
                return versionName;
            }

            return "";
        } catch (final PackageManager.NameNotFoundException ignored) {
            return "";
        }
    }

    public static boolean isGoogleRecognizerAvailable(final Context context) {
        return findGoogleRecognizer(context) != null;
        //return false;
    }

    private static String getPackageName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return GOOGLE_RECOGNIZER_PACKAGE_NAME;
        } else {
            return GOOGLE_VOICE_SEARCH_PACKAGE_NAME;
        }
    }

}
