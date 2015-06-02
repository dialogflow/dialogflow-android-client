package ai.api.sample;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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

public class AISettingsActivity extends BaseActivity implements
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private CheckBox bluetoothSwitch;

    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsManager = ((AIApplication) getApplication()).getSettingsManager();

        ViewGroup bluetoothSection = (ViewGroup) findViewById(R.id.activity_settings_bluetooth_section);
        bluetoothSection.setOnClickListener(this);

        bluetoothSwitch = (CheckBox) findViewById(R.id.activity_settings_bluetooth_swith);
        bluetoothSwitch.setChecked(settingsManager.isUseBluetooth());
        bluetoothSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_settings_bluetooth_section:
                bluetoothSwitch.performClick();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.activity_settings_bluetooth_swith:
                settingsManager.setUseBluetooth(isChecked);
                break;
        }
    }
}
