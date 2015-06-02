package ai.api.sample;

import android.content.Context;
import android.content.SharedPreferences;

import ai.api.util.BluetoothController;

/***********************************************************************************************************************
 * API.AI Android SDK -  API.AI libraries usage example
 * =================================================
 * <p/>
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 * <p/>
 * **********************************************************************************************************************
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***********************************************************************************************************************/

public class SettingsManager {

    private static final String SETTINGS_PREFS_NAME = "ai.api.APP_SETTINGS";
    private static final String PREF_USE_BLUETOOTH = "USE_BLUETOOTH";

    private final Context context;
    private SharedPreferences prefs;

    private boolean useBluetooth;

    public SettingsManager(final Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);

        useBluetooth = prefs.getBoolean(PREF_USE_BLUETOOTH, true);
    }

    public void setUseBluetooth(final boolean useBluetooth) {
        this.useBluetooth = useBluetooth;

        prefs.edit().putBoolean(PREF_USE_BLUETOOTH, useBluetooth).commit();
        final BluetoothController controller = ((AIApplication) context.getApplicationContext()).getBluetoothController();
        if (useBluetooth) {
            controller.start();
        } else {
            controller.stop();
        }
    }

    public boolean isUseBluetooth() {
        return useBluetooth;
    }

}
