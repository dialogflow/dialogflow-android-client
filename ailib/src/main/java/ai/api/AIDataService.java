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

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import ai.api.http.HttpClient;
import ai.api.model.AIContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

/**
 * Do simple requests to the AI Service
 */
public class AIDataService {

    public static final String TAG = AIDataService.class.getName();

    private final Context context;
    private final AIConfiguration config;

    private final String sessionId;

    public AIDataService(final Context context, final AIConfiguration config) {
        this.context = context;
        this.config = config;

        sessionId = UUID.randomUUID().toString();
    }

    /**
     * Make request to the ai service. This method must not be called in the UI Thread
     *
     * @param request request object to the service
     * @return response object from service
     */
    public AIResponse request(final AIRequest request) throws AIServiceException {
        if (request == null) {
            throw new IllegalArgumentException("Request argument must not be null");
        }

        Log.d(TAG, "Start request");

        final Gson gson = GsonFactory.getGson();

        HttpURLConnection connection = null;

        try {
            final URL url = new URL(config.getQuestionUrl());

            request.setLanguage(config.getLanguage());
            request.setSessionId(sessionId);
            request.setTimezone(Calendar.getInstance().getTimeZone().getID());

            final String queryData = gson.toJson(request);

            Log.d(TAG, "Request json: " + queryData);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.addRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.addRequestProperty("ocp-apim-subscription-key", config.getSubscriptionKey());
            connection.addRequestProperty("Content-Type","application/json; charset=utf-8");
            connection.addRequestProperty("Accept","application/json");

            connection.connect();

            final BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            IOUtils.write(queryData, outputStream, Charsets.UTF_8);
            outputStream.close();

            final InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            final String response = IOUtils.toString(inputStream, Charsets.UTF_8);
            inputStream.close();

            if (TextUtils.isEmpty(response)) {
                throw new AIServiceException("Empty response from ai service. Please check configuration.");
            }

            Log.d(TAG, "Response json: " + response);

            final AIResponse aiResponse = gson.fromJson(response, AIResponse.class);

            return aiResponse;

        } catch (final MalformedURLException e) {
            Log.e(TAG, "Malformed url should not be raised", e);
            throw new AIServiceException("Wrong configuration. Please, connect to AI Service support", e);
        } catch (final IOException e) {

            if (connection != null) {
                try {
                    final String errorString = IOUtils.toString(connection.getErrorStream(), Charsets.UTF_8);
                    Log.d(TAG, "" + errorString);
                    if (!TextUtils.isEmpty(errorString)) {
                        final AIResponse errorResponse = gson.fromJson(errorString, AIResponse.class);
                        return errorResponse;
                    }
                } catch (final IOException ex) {
                    Log.w(TAG, "Wrong error format", ex);
                }
            }

            Log.e(TAG, "Can't make request to the Speaktoit AI service. Please, check connection settings and API access token.", e);
            throw new AIServiceException("Can't make request to the AI service. Please, check connection settings and API access token.", e);
        } catch (final JsonSyntaxException je) {
            throw new AIServiceException("Wrong service answer format. Please, connect to AI Service support", je);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    /**
     * Make requests to the ai service with voiceData.  This method must not be called in the UI Thread.
     * @param voiceStream voice data stream for recognition
     * @return response object from service
     * @throws AIServiceException
     */
    public AIResponse voiceRequest(final InputStream voiceStream, final List<AIContext> aiContexts) throws AIServiceException {
        final Gson gson = GsonFactory.getGson();

        HttpURLConnection connection = null;

        HttpClient httpClient = null;

        Log.d(TAG, "Start voice request");

        try {
            final AIRequest request = new AIRequest();

            final URL url = new URL(config.getQuestionUrl());

            request.setLanguage(config.getLanguage());
            request.setSessionId(sessionId);
            request.setTimezone(Calendar.getInstance().getTimeZone().getID());

            if (context != null) {
                request.setContexts(aiContexts);
            }

            final String queryData = gson.toJson(request);

            Log.d(TAG, "Request json: " + queryData);

            connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.addRequestProperty("ocp-apim-subscription-key", config.getSubscriptionKey());
            connection.addRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            httpClient = new HttpClient(connection);
            httpClient.setWriteSoundLog(config.isWriteSoundLog());

            httpClient.connectForMultipart();
            httpClient.addFormPart("request", queryData);
            httpClient.addFilePart("voiceData", "voice.wav", voiceStream);
            httpClient.finishMultipart();

            final String response = httpClient.getResponse();

            if (TextUtils.isEmpty(response)) {
                throw new AIServiceException("Empty response from ai service. Please check configuration.");
            }

            Log.d(TAG, "Response json: " + response);

            final AIResponse aiResponse = gson.fromJson(response, AIResponse.class);
            return aiResponse;

        } catch (final MalformedURLException e) {
            Log.e(TAG, "Malformed url should not be raised", e);
            throw new AIServiceException("Wrong configuration. Please, connect to AI Service support", e);
        } catch (final IOException e) {

            if (httpClient != null) {
                final String errorString = httpClient.getErrorString();
                Log.d(TAG, "" + errorString);
                if (!TextUtils.isEmpty(errorString)) {
                    final AIResponse errorResponse = gson.fromJson(errorString, AIResponse.class);
                    return errorResponse;
                }
            }

            Log.e(TAG, "Can't make request to the Speaktoit AI service. Please, check connection settings and API access token.", e);
            throw new AIServiceException("Can't make request to the AI service. Please, check connection settings and API access token.", e);
        } catch (final JsonSyntaxException je) {
            throw new AIServiceException("Wrong service answer format. Please, connect to AI Service support", je);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
