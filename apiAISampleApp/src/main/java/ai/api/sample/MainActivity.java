package ai.api.sample;

/***********************************************************************************************************************
 *
 * API.AI Android SDK -  API.AI libraries usage example
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

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import ai.api.AIConfiguration;
import ai.api.AIListener;
import ai.api.AIService;
import ai.api.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;

import java.util.Locale;


public class MainActivity extends ActionBarActivity implements AIListener {

    private static final String ACCESS_TOKEN = "INSERT_CLIENT_ACCESS_TOKEN_HERE";
    private AIService aiService;
    private ProgressBar progressBar;
    private ImageView recIndicator;
    private TextView resultTextView;
    private Gson gson;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        recIndicator = (ImageView) findViewById(R.id.recIndicator);
        recIndicator.setVisibility(View.INVISIBLE);

        resultTextView = (TextView) findViewById(R.id.resultTextView);

        gson = GsonFactory.getGson();

        final AIConfiguration config = new AIConfiguration(ACCESS_TOKEN, Locale.US.toString(), AIConfiguration.RecognitionEngine.Google);
        config.setDebug(true);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void buttonListenOnClick(final View view) {
        aiService.startListening();
    }

    public void buttonStopListenOnClick(final View view) {
        aiService.stopListening();
    }

    public void buttonCancelOnClick(final View view) {
        aiService.cancel();
    }

    @Override
    public void onResult(final AIResponse result) {
        if (result.isError()) {
            resultTextView.setText("Error: " + result.getStatus().getErrorDetails());
        } else {
            resultTextView.setText(gson.toJson(result));
        }
    }

    @Override
    public void onError(final AIError error) {
        resultTextView.setText(error.toString());
    }

    @Override
    public void onAudioLevel(final float level) {
        float positiveLevel = Math.abs(level);
        if (positiveLevel > 100) {
            positiveLevel = 100;
        }

        progressBar.setProgress((int) positiveLevel);
    }

    @Override
    public void onListeningStarted() {
        recIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onListeningFinished() {
        recIndicator.setVisibility(View.INVISIBLE);
    }
}
