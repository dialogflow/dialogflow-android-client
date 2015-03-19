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

import android.support.v7.app.ActionBarActivity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.api.AIConfiguration;
import ai.api.AIListener;
import ai.api.AIService;
import ai.api.GsonFactory;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIResponse;

public class AIServiceSampleActivity extends ActionBarActivity implements AIListener {

    public static final String TAG = AIServiceSampleActivity.class.getName();


    private AIService aiService;
    private ProgressBar progressBar;
    private ImageView recIndicator;
    private TextView resultTextView;
    private Gson gson;
    private EditText contextTextView;
    private Spinner selectLanguageSpinner;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aiservice_sample);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        recIndicator = (ImageView) findViewById(R.id.recIndicator);
        recIndicator.setVisibility(View.INVISIBLE);

        resultTextView = (TextView) findViewById(R.id.resultTextView);
        contextTextView = (EditText) findViewById(R.id.contextTextView);
        selectLanguageSpinner = (Spinner) findViewById(R.id.selectLanguageSpinner);

        final LanguageConfig[] languages = new LanguageConfig[] {
                new LanguageConfig("en","327bf2eb54904e508362f6fb528ce00a"),
                new LanguageConfig("ru","adcb900f02594f4186420c082e44173e"),
                new LanguageConfig("de","96807aac0e98426eaf684f4081b7e431"),
                new LanguageConfig("pt","4c4a2277516041f6a1c909163ebfed39"),
                new LanguageConfig("pt-BR", "6076377eea9e4291854204795b55eee9"),
                new LanguageConfig("es", "430d461ea8e64c09a4459560353a5b1d"),
                new LanguageConfig("fr","d6434b3bf49d4a93a25679782619f39d"),
                new LanguageConfig("it","4319f7aa1765468194d9761432e4db36"),
                new LanguageConfig("ja", "6cab6813dc8c416f92c3c2e2b4a7bc27"),
                new LanguageConfig("ko", "b0219c024ee848eaa7cfb17dceb9934a"),
                new LanguageConfig("zh-CN","2b575c06deb246d2abe4bf769eb3200b"),
                new LanguageConfig("zh-HK", "00ef32d3e35f43178405c046a16f3843"),
                new LanguageConfig("zh-TW", "dd7ebc8a02974155aeffec26b21b55cf"),
        };

        final ArrayAdapter<LanguageConfig> languagesAdapter = new ArrayAdapter<LanguageConfig>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        selectLanguageSpinner.setAdapter(languagesAdapter);
        selectLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                final LanguageConfig selectedLanguage = (LanguageConfig) parent.getItemAtPosition(position);
                initService(selectedLanguage);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        gson = GsonFactory.getGson();
    }

    private void initService(final LanguageConfig selectedLanguage) {
        final AIConfiguration.SupportedLanguages lang = AIConfiguration.SupportedLanguages.fromLanguageTag(selectedLanguage.getLanguageCode());
        final AIConfiguration config = new AIConfiguration(selectedLanguage.getAccessToken(),
                "62f2522a-7404-4c28-b9ac-097ca5d8b32d", lang,
                AIConfiguration.RecognitionEngine.System);

        if (aiService != null) {
            aiService.pause();
        }

        aiService = AIService.getService(this, config);
        aiService.setListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_aiservice_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

        Log.d(TAG, "onPause");

        // use this method to disconnect from speech recognition service
        // Not destroying the SpeechRecognition object in onPause method would block other apps from using SpeechRecognition service
        if (aiService != null) {
            aiService.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

        // use this method to reinit connection to recognition service
        if (aiService != null) {
            aiService.resume();
        }
    }

    public void buttonListenOnClick(final View view) {
        final String contextString = String.valueOf(contextTextView.getText());
        if (!TextUtils.isEmpty(contextString)) {

            final AIContext aiContext = new AIContext(contextString);
            final List<AIContext> contexts = new ArrayList<AIContext>();
            contexts.add(aiContext);
            aiService.startListening(contexts);

        } else {
            aiService.startListening();
        }

    }

    public void buttonStopListenOnClick(final View view) {
        aiService.stopListening();
    }

    public void buttonCancelOnClick(final View view) {
        aiService.cancel();
    }

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
                    resultTextView.setText(gson.toJson(response));

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
                        for (final Map.Entry<String, JsonElement> entry : response.getResult().getParameters().entrySet()){
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

                Log.d(TAG, "Sound level:" + level);
            }
        });
    }

    @Override
    public void onListeningStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onListeningStarted");

                recIndicator.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onListeningFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onListeningFinished");

                recIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

}
