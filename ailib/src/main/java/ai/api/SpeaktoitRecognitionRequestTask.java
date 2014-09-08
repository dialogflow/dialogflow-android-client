package ai.api;

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

import android.os.AsyncTask;
import android.util.Log;

import ai.api.http.HttpClient;
import ai.api.model.QuestionMetadata;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpeaktoitRecognitionRequestTask extends AsyncTask<SpeaktoitRecognitionRequest, Integer, String> {

    public static final String TAG = SpeaktoitRecognitionRequestTask.class.getName();

    private final URL url;

    public SpeaktoitRecognitionRequestTask(final URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url must not be null");
        }

        this.url = url;
    }

    @Override
    protected String doInBackground(final SpeaktoitRecognitionRequest... params) {

        final SpeaktoitRecognitionRequest speaktoitRequest = params[0];

        final byte[] soundData = speaktoitRequest.getSoundData();
        final QuestionMetadata metadata = speaktoitRequest.getMetadata();

        final String metadataString = GsonFactory.getGson().toJson(metadata);

        final String response;
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();

            final HttpClient httpClient = new HttpClient(connection);
            httpClient.connectForMultipart();

            httpClient.addFilePart("sound","sound.wav",soundData);
            httpClient.addFormPart("metadata", metadataString);

            httpClient.finishMultipart();

            response = httpClient.getResponse();
            return response;

        } catch (final IOException e) {
            Log.e(TAG, "Can't make request to the Speaktoit AI service. Please, check connection settings and API access token.", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }
}
