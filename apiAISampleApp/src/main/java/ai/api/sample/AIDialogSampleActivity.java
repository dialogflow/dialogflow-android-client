package ai.api.sample;

/***********************************************************************************************************************
 *
 * API.AI Android SDK -  API.AI libraries usage example
 * =================================================
 *
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.Map;

import ai.api.AIConfiguration;
import ai.api.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIDialog;

public class AIDialogSampleActivity extends ActionBarActivity {

    private static final String TAG = AIDialogSampleActivity.class.getName();

    private TextView resultTextView;
    private AIDialog aiDialog;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidialog_sample);

        resultTextView = (TextView) findViewById(R.id.resultTextView);

        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                Config.SUBSCRIPTION_KEY, AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        config.setExperimental(true);

        aiDialog = new AIDialog(this, config);
        aiDialog.setResultsListener(new AIDialog.AIDialogListener() {
            @Override
            public void onResult(final AIResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onResult");

                        if (response.isError()) {
                            resultTextView.setText("Error: " + response.getStatus().getErrorDetails());

                            Log.i(TAG, "Received error response");

                            // this is example how to get different parts of error description
                            Log.i(TAG, "Error details: " + response.getStatus().getErrorDetails());
                            Log.i(TAG, "Error type: " + response.getStatus().getErrorType());
                        } else {
                            resultTextView.setText(GsonFactory.getGson().toJson(response));

                            Log.i(TAG, "Received success response");

                            // this is example how to get different parts of result object
                            Log.i(TAG, "Status code: " + response.getStatus().getCode());
                            Log.i(TAG, "Status type: " + response.getStatus().getErrorType());

                            Log.i(TAG, "Resolved query: " + response.getResult().getResolvedQuery());

                            Log.i(TAG, "Action: " + response.getResult().getAction());
                            Log.i(TAG, "Speech: " + response.getResult().getSpeech());

                            if (response.getResult().getMetadata() != null) {
                                Log.i(TAG, "Intent id: " + response.getResult().getMetadata().getIntentId());
                                Log.i(TAG, "Intent name: " + response.getResult().getMetadata().getIntentName());
                            }

                            if (response.getResult().getParameters() != null && !response.getResult().getParameters().isEmpty()) {
                                Log.i(TAG, "Parameters: ");
                                for (final Map.Entry<String, JsonElement> entry : response.getResult().getParameters().entrySet()) {
                                    Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(final AIError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onError");
                        resultTextView.setText(error.toString());
                    }
                });
            }
        });
    }

    public void buttonListenOnClick(final View view) {
        aiDialog.showAndListen();
    }
}
