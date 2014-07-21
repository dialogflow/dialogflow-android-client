package com.speaktoit.ai;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Request task used only to make string request to the AI service and get string as response without decoding it
 */
class RequestTask extends AsyncTask<String, Integer, String> {

    private static final String TAG = RequestTask.class.getName();

    private final URL url;
    private final String accessToken;

    RequestTask(final URL url, final String accessToken) {
        if (url == null) {
            throw new IllegalArgumentException("url must not be null");
        }
        this.url = url;
        this.accessToken = accessToken;

    }

    @Override
    protected String doInBackground(final String... params) {
        final String payload = params[0];
        if (TextUtils.isEmpty(payload)) {
            throw new IllegalArgumentException("payload argument should not be empty");
        }

        String response = null;
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.addRequestProperty("Authorization","Bearer " + accessToken);

            connection.connect();

            final BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            IOUtils.write(payload, outputStream, Charsets.UTF_8);
            outputStream.close();

            final InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            response = IOUtils.toString(inputStream, Charsets.UTF_8);
            inputStream.close();

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
