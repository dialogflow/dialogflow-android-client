package com.speaktoit.ai;

import android.os.AsyncTask;
import android.util.Log;

import com.speaktoit.ai.http.HttpClient;
import com.speaktoit.ai.model.QuestionMetadata;

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
