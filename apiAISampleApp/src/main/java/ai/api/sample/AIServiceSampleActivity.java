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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.android.AIConfiguration;
import ai.api.AIListener;
import ai.api.android.AIService;
import ai.api.android.GsonFactory;
import ai.api.RequestExtras;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;

public class AIServiceSampleActivity extends BaseActivity
        implements AIListener, AdapterView.OnItemSelectedListener {

    public static final String TAG = AIServiceSampleActivity.class.getName();


    private AIService aiService;
    private ProgressBar progressBar;
    private ImageView recIndicator;
    private TextView resultTextView;
    private EditText contextEditText;

    private Gson gson = GsonFactory.getGson();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aiservice_sample);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recIndicator = (ImageView) findViewById(R.id.recIndicator);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        contextEditText = (EditText) findViewById(R.id.contextEditText);

        Spinner spinner = (Spinner) findViewById(R.id.selectLanguageSpinner);
        final ArrayAdapter<LanguageConfig> languagesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Config.languages);
        spinner.setAdapter(languagesAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void initService(final LanguageConfig selectedLanguage) {
        final AIConfiguration.SupportedLanguages lang = AIConfiguration.SupportedLanguages.fromLanguageTag(selectedLanguage.getLanguageCode());
        final AIConfiguration config = new AIConfiguration(selectedLanguage.getAccessToken(),
                lang,
                AIConfiguration.RecognitionEngine.System);

        if (aiService != null) {
            aiService.pause();
        }

        aiService = AIService.getService(this, config);
        aiService.setListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_aiservice_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // use this method to disconnect from speech recognition service
        // Not destroying the SpeechRecognition object in onPause method would block other apps from using SpeechRecognition service
        if (aiService != null) {
            aiService.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // use this method to reinit connection to recognition service
        if (aiService != null) {
            aiService.resume();
        }
    }

    public void startRecognition(final View view) {
        final String contextString = String.valueOf(contextEditText.getText());
        if (TextUtils.isEmpty(contextString)) {
            aiService.startListening();
        } else {
            final List<AIContext> contexts = Collections.singletonList(new AIContext(contextString));
            final RequestExtras requestExtras = new RequestExtras(contexts, null);
            aiService.startListening(requestExtras);
        }

    }

    public void stopRecognition(final View view) {
        aiService.stopListening();
    }

    public void cancelRecognition(final View view) {
        aiService.cancel();
    }

    @Override
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onResult");

                resultTextView.setText(gson.toJson(response));

                Log.i(TAG, "Received success response");

                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());

                final String speech = result.getFulfillment().getSpeech();
                Log.i(TAG, "Speech: " + speech);
                TTS.speak(speech);

                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
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
                resultTextView.setText(error.toString());
            }
        });
    }

    @Override
    public void onAudioLevel(final float level) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float positiveLevel = Math.abs(level);

                if (positiveLevel > 100) {
                    positiveLevel = 100;
                }
                progressBar.setProgress((int) positiveLevel);
            }
        });
    }

    @Override
    public void onListeningStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recIndicator.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onListeningCanceled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recIndicator.setVisibility(View.INVISIBLE);
                resultTextView.setText("");
            }
        });
    }

    @Override
    public void onListeningFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final LanguageConfig selectedLanguage = (LanguageConfig) parent.getItemAtPosition(position);
        initService(selectedLanguage);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
