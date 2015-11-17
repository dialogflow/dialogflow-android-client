package ai.api;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 *
 * *********************************************************************************************************************
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ai.api.http.HttpClient;
import ai.api.model.AIContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Entity;
import ai.api.model.Status;

/**
 * Do simple requests to the AI Service
 */
public class AIDataService {

    public static final String TAG = AIDataService.class.getName();

    @NonNull
    private final Context context;

    @NonNull
    private final AIConfiguration config;

    @NonNull
    private final String sessionId;

    @NonNull
    private final Gson gson = GsonFactory.getGson();

    public AIDataService(@NonNull final Context context, @NonNull final AIConfiguration config) {
        this.context = context;
        this.config = config;

        sessionId = SessionIdStorage.getSessionId(context);
    }

    public AIResponse request(@NonNull final AIRequest request) throws AIServiceException {
        return request(request, null);
    }

    /**
     * Make request to the ai service. This method must not be called in the UI Thread
     *
     * @param request request object to the service
     * @return response object from service
     */
    @NonNull
    public AIResponse request(@NonNull final AIRequest request, @Nullable final RequestExtras requestExtras) throws AIServiceException {
        if (request == null) {
            throw new IllegalArgumentException("Request argument must not be null");
        }

        Log.d(TAG, "Start request");

        try {

            request.setLanguage(config.getLanguage());
            request.setSessionId(sessionId);
            request.setTimezone(Calendar.getInstance().getTimeZone().getID());

            Map<String, String> additionalHeaders = null;

            if (requestExtras != null) {
                if (requestExtras.hasContexts()) {
                    request.setContexts(requestExtras.getContexts());
                }

                if (requestExtras.hasEntities()) {
                    request.setEntities(requestExtras.getEntities());
                }

                additionalHeaders = requestExtras.getAdditionalHeaders();
            }

            final String queryData = gson.toJson(request);

            Log.d(TAG, "Request json: " + queryData);

            final String response = doTextRequest(config.getQuestionUrl(), queryData, additionalHeaders);

            if (TextUtils.isEmpty(response)) {
                throw new AIServiceException("Empty response from ai service. Please check configuration and Internet connection.");
            }

            Log.d(TAG, "Response json: " + response);

            final AIResponse aiResponse = gson.fromJson(response, AIResponse.class);

            if (aiResponse == null) {
                throw new AIServiceException("API.AI response parsed as null. Check debug log for details.");
            }

            if (aiResponse.isError()) {
                throw new AIServiceException(aiResponse);
            }

            aiResponse.cleanup();

            return aiResponse;

        } catch (final MalformedURLException e) {
            Log.e(TAG, "Malformed url should not be raised", e);
            throw new AIServiceException("Wrong configuration. Please, connect to API.AI Service support", e);
        } catch (final JsonSyntaxException je) {
            throw new AIServiceException("Wrong service answer format. Please, connect to API.AI Service support", je);
        }

    }

    /**
     * Make requests to the ai service with voice data. This method must not be called in the UI Thread.
     *
     * @param voiceStream voice data stream for recognition
     * @return response object from service
     * @throws AIServiceException
     */
    @NonNull
    public AIResponse voiceRequest(@NonNull final InputStream voiceStream) throws AIServiceException {
        return voiceRequest(voiceStream, new RequestExtras());
    }

    /**
     * Make requests to the ai service with voice data. This method must not be called in the UI Thread.
     *
     * @param voiceStream voice data stream for recognition
     * @param aiContexts additional contexts for request
     * @return response object from service
     * @throws AIServiceException
     */
    @NonNull
    public AIResponse voiceRequest(@NonNull final InputStream voiceStream, @Nullable final List<AIContext> aiContexts) throws AIServiceException {
        return voiceRequest(voiceStream, new RequestExtras(aiContexts, null));
    }

    /**
     * Make requests to the ai service with voice data. This method must not be called in the UI Thread.
     *
     * @param voiceStream voice data stream for recognition
     * @param requestExtras object that can hold additional contexts and entities
     * @return response object from service
     * @throws AIServiceException
     */
    @NonNull
    public AIResponse voiceRequest(@NonNull final InputStream voiceStream, @Nullable final RequestExtras requestExtras) throws AIServiceException {
        Log.d(TAG, "Start voice request");

        try {
            final AIRequest request = new AIRequest();

            request.setLanguage(config.getLanguage());
            request.setSessionId(sessionId);
            request.setTimezone(Calendar.getInstance().getTimeZone().getID());

            Map<String, String> additionalHeaders = null;

            if (requestExtras != null) {
                if (requestExtras.hasContexts()) {
                    request.setContexts(requestExtras.getContexts());
                }
                if (requestExtras.hasEntities()) {
                    request.setEntities(requestExtras.getEntities());
                }

                additionalHeaders = requestExtras.getAdditionalHeaders();
            }

            final String queryData = gson.toJson(request);

            Log.d(TAG, "Request json: " + queryData);

            final String response = doSoundRequest(voiceStream, queryData, additionalHeaders);

            if (TextUtils.isEmpty(response)) {
                throw new AIServiceException("Empty response from ai service. Please check configuration.");
            }

            Log.d(TAG, "Response json: " + response);

            final AIResponse aiResponse = gson.fromJson(response, AIResponse.class);

            if (aiResponse == null) {
                throw new AIServiceException("API.AI response parsed as null. Check debug log for details.");
            }

            if (aiResponse.isError()) {
                throw new AIServiceException(aiResponse);
            }

            aiResponse.cleanup();

            return aiResponse;

        } catch (final MalformedURLException e) {
            Log.e(TAG, "Malformed url should not be raised", e);
            throw new AIServiceException("Wrong configuration. Please, connect to AI Service support", e);
        } catch (final JsonSyntaxException je) {
            throw new AIServiceException("Wrong service answer format. Please, connect to API.AI Service support", je);
        }
    }

    /**
     * Forget all old contexts
     *
     * @return true if operation succeed, false otherwise
     */
    public boolean resetContexts() {
        final AIRequest cleanRequest = new AIRequest();
        cleanRequest.setQuery("empty_query_for_resetting_contexts"); // TODO remove it after protocol fix
        cleanRequest.setResetContexts(true);
        try {
            final AIResponse response = request(cleanRequest);
            return !response.isError();
        } catch (final AIServiceException e) {
            Log.e(TAG, "Exception while contexts clean.", e);
            return false;
        }
    }

    public AIResponse uploadUserEntity(final Entity userEntity) throws AIServiceException {
        return uploadUserEntities(Collections.singleton(userEntity));
    }

    public AIResponse uploadUserEntities(final Collection<Entity> userEntities) throws AIServiceException {
        if (userEntities == null || userEntities.size() == 0) {
            throw new AIServiceException("Empty entities list");
        }

        final String requestData = gson.toJson(userEntities);
        try {
            final String response = doTextRequest(config.getUserEntitiesEndpoint(sessionId), requestData);
            if (TextUtils.isEmpty(response)) {
                throw new AIServiceException("Empty response from ai service. Please check configuration and Internet connection.");
            }
            Log.d(TAG, "Response json: " + response);

            final AIResponse aiResponse = gson.fromJson(response, AIResponse.class);

            if (aiResponse == null) {
                throw new AIServiceException("API.AI response parsed as null. Check debug log for details.");
            }

            if (aiResponse.isError()) {
                throw new AIServiceException(aiResponse);
            }

            aiResponse.cleanup();
            return aiResponse;

        } catch (final MalformedURLException e) {
            Log.e(TAG, "Malformed url should not be raised", e);
            throw new AIServiceException("Wrong configuration. Please, connect to AI Service support", e);
        } catch (final JsonSyntaxException je) {
            throw new AIServiceException("Wrong service answer format. Please, connect to API.AI Service support", je);
        }
    }

    protected String doTextRequest(final String requestJson) throws MalformedURLException, AIServiceException {
        return doTextRequest(config.getQuestionUrl(), requestJson);
    }

    protected String doTextRequest(final String endpoint, final String requestJson) throws MalformedURLException, AIServiceException {
        return doTextRequest(endpoint, requestJson, null);
    }

    protected String doTextRequest(@NonNull final String endpoint,
                                   @NonNull final String requestJson, 
                                   @Nullable final Map<String, String> additionalHeaders) throws MalformedURLException, AIServiceException {

        HttpURLConnection connection = null;

        try {

            final URL url = new URL(endpoint);

            final String queryData = requestJson;

            Log.d(TAG, "Request json: " + queryData);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.addRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.addRequestProperty("ocp-apim-subscription-key", config.getSubscriptionKey());
            connection.addRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.addRequestProperty("Accept", "application/json");

            if (additionalHeaders != null) {
                for (final Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                    connection.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            connection.connect();

            final BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            IOUtils.write(queryData, outputStream, Charsets.UTF_8);
            outputStream.close();

            final InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            final String response = IOUtils.toString(inputStream, Charsets.UTF_8);
            inputStream.close();

            return response;
        } catch (final IOException e) {
            if (connection != null) {
                try {
                    final InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        final String errorString = IOUtils.toString(errorStream, Charsets.UTF_8);
                        Log.d(TAG, "" + errorString);
                        return errorString;
                    }
                    else {
                        throw new AIServiceException("Can't connect to the api.ai service.", e);
                    }
                } catch (final IOException ex) {
                    Log.w(TAG, "Can't read error response", ex);
                }
            }
            Log.e(TAG, "Can't make request to the API.AI service. Please, check connection settings and API access token.", e);
            throw new AIServiceException("Can't make request to the API.AI service. Please, check connection settings and API access token.", e);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    protected String doSoundRequest(@NonNull final InputStream voiceStream, @NonNull final String queryData) throws MalformedURLException, AIServiceException {
        return doSoundRequest(voiceStream, queryData, null);
    }

    /**
     * Method extracted for testing purposes
     */
    protected String doSoundRequest(@NonNull final InputStream voiceStream,
                                    @NonNull final String queryData,
                                    @Nullable final Map<String, String> additionalHeaders) throws MalformedURLException, AIServiceException {

        HttpURLConnection connection = null;
        HttpClient httpClient = null;

        try {
            final URL url = new URL(config.getQuestionUrl());

            connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.addRequestProperty("ocp-apim-subscription-key", config.getSubscriptionKey());
            connection.addRequestProperty("Accept", "application/json");

            if (additionalHeaders != null) {
                for (final Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                    connection.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }

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
            return response;

        } catch (final IOException e) {

            if (httpClient != null) {
                final String errorString = httpClient.getErrorString();
                Log.d(TAG, "" + errorString);
                if (!TextUtils.isEmpty(errorString)) {
                    return errorString;
                } else if (e instanceof HttpRetryException) {
                    final AIResponse response = new AIResponse();
                    final int code = ((HttpRetryException) e).responseCode();
                    final Status status = Status.fromResponseCode(code);
                    status.setErrorDetails(((HttpRetryException) e).getReason());
                    response.setStatus(status);
                    throw new AIServiceException(response);
                }
            }

            Log.e(TAG, "Can't make request to the API.AI service. Please, check connection settings and API.AI keys.", e);
            throw new AIServiceException("Can't make request to the API.AI service. Please, check connection settings and API.AI keys.", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
