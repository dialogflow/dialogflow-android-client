package ai.api.services;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
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

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.AIConfiguration;
import ai.api.AIService;
import ai.api.AIServiceException;
import ai.api.PartialResultsListener;
import ai.api.RequestExtras;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.util.RecognizerChecker;

public class GoogleRecognitionServiceImpl extends AIService {

    private static final String TAG = GoogleRecognitionServiceImpl.class.getName();

    private SpeechRecognizer speechRecognizer;
    private final Object speechRecognizerLock = new Object();
    private RequestExtras requestExtras;
    private PartialResultsListener partialResultsListener;

    private volatile boolean recognitionActive = false;
    private volatile boolean wasReadyForSpeech;

    private final Map<Integer, String> errorMessages = new HashMap<>();

    {
        errorMessages.put(SpeechRecognizer.ERROR_NETWORK_TIMEOUT, "Network operation timed out.");
        errorMessages.put(SpeechRecognizer.ERROR_NETWORK, "Other network related errors.");
        errorMessages.put(SpeechRecognizer.ERROR_AUDIO, "Audio recording error.");
        errorMessages.put(SpeechRecognizer.ERROR_SERVER, "Server sends error status.");
        errorMessages.put(SpeechRecognizer.ERROR_CLIENT, "Other client side errors.");
        errorMessages.put(SpeechRecognizer.ERROR_SPEECH_TIMEOUT, "No speech input.");
        errorMessages.put(SpeechRecognizer.ERROR_NO_MATCH, "No recognition result matched.");
        errorMessages.put(SpeechRecognizer.ERROR_RECOGNIZER_BUSY, "RecognitionService busy.");
        errorMessages.put(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS, "Insufficient permissions.");
    }


    public GoogleRecognitionServiceImpl(final Context context, final AIConfiguration config) {
        super(config, context);

        final ComponentName component = RecognizerChecker.findGoogleRecognizer(context);
        if (component == null) {
            Log.w(TAG, "Google Recognizer application not found on device. " +
                    "Quality of the recognition may be low. Please check if Google Search application installed and enabled.");
        }
    }

    protected void initializeRecognizer() {
        if (speechRecognizer != null) {
            return;
        }

        synchronized (speechRecognizerLock) {
            if (speechRecognizer != null) {
                speechRecognizer.destroy();
                speechRecognizer = null;
            }

            final ComponentName component = RecognizerChecker.findGoogleRecognizer(context);
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context, component);
            speechRecognizer.setRecognitionListener(new InternalRecognitionListener());
        }
    }

    protected void clearRecognizer() {
        if (speechRecognizer != null) {
            synchronized (speechRecognizerLock) {
                if (speechRecognizer != null) {
                    speechRecognizer.destroy();
                    speechRecognizer = null;
                }
            }
        }
    }

    private void sendRequest(@NonNull final AIRequest aiRequest, @Nullable final RequestExtras requestExtras) {
        if (aiRequest == null) {
            throw new IllegalArgumentException("aiRequest must be not null");
        }

        final AsyncTask<AIRequest, Integer, AIResponse> task = new AsyncTask<AIRequest, Integer, AIResponse>() {

            private AIError aiError;

            @Override
            protected AIResponse doInBackground(final AIRequest... params) {
                final AIRequest request = params[0];
                try {
                    return aiDataService.request(request, requestExtras);
                } catch (final AIServiceException e) {
                    aiError = new AIError(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final AIResponse response) {
                if (response != null) {
                    onResult(response);
                } else {
                    onError(aiError);
                }
            }
        };

        task.execute(aiRequest);
    }

    @Override
    public void startListening() {
        startListening(new RequestExtras());
    }

    @Override
    public void startListening(final List<AIContext> contexts) {
        startListening(new RequestExtras(contexts, null));
    }

    @Override
    public void startListening(final RequestExtras requestExtras) {
        if (!recognitionActive) {
            synchronized (speechRecognizerLock) {
                this.requestExtras = requestExtras;

                initializeRecognizer();

                recognitionActive = true;

                final Intent sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                final String language = config.getLanguage().replace('-', '_');

                sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
                sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
                sttIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

                // WORKAROUND for https://code.google.com/p/android/issues/detail?id=75347
                // TODO Must be removed after fix in Android
                sttIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{});

                try {
                    wasReadyForSpeech = false;
                    speechRecognizer.startListening(sttIntent);
                } catch (final SecurityException e) { //Error occurs only on HTC devices.
                }
            }
        } else {
            Log.w(TAG, "Trying to start recognition while another recognition active");
            if (!wasReadyForSpeech) {
                cancel();
            }
        }
    }

    @Override
    public void stopListening() {
        synchronized (speechRecognizerLock) {
            if (speechRecognizer != null) {
                speechRecognizer.stopListening();
            }
        }
    }

    @Override
    public void cancel() {
        synchronized (speechRecognizerLock) {
            if (recognitionActive) {
                recognitionActive = false;
                if (speechRecognizer != null) {
                    speechRecognizer.cancel();
                }
                onListeningCancelled();
            }
        }
    }

    /**
     * This method must be called from UI thread
     */
    @Override
    public void pause() {
        clearRecognizer();
    }

    /**
     * This method must be called from UI thread
     */
    @Override
    public void resume() {
    }

    public void setPartialResultsListener(PartialResultsListener partialResultsListener) {
        this.partialResultsListener = partialResultsListener;
    }

    protected void onPartialResults(final List<String> partialResults) {
        if (partialResultsListener != null) {
            partialResultsListener.onPartialResults(partialResults);
        }
    }

    private class InternalRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(final Bundle params) {
            if (recognitionActive) {
                onListeningStarted();
            }
            wasReadyForSpeech = true;
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(final float rmsdB) {
            if (recognitionActive) {
                onAudioLevelChanged(rmsdB);
            }
        }

        @Override
        public void onBufferReceived(final byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            if (recognitionActive) {
                onListeningFinished();
            }
        }

        @Override
        public void onError(final int error) {
            if (error == SpeechRecognizer.ERROR_NO_MATCH && !wasReadyForSpeech) {
                return;
            }

            if (recognitionActive) {
                final AIError aiError;

                if (errorMessages.containsKey(error)) {
                    final String description = errorMessages.get(error);
                    aiError = new AIError("Speech recognition engine error: " + description);
                } else {
                    aiError = new AIError("Speech recognition engine error: " + error);
                }

                GoogleRecognitionServiceImpl.this.onError(aiError);
            }

            recognitionActive = false;
        }

        @TargetApi(14)
        @Override
        public void onResults(final Bundle results) {
            if (recognitionActive) {
                final ArrayList<String> recognitionResults = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                float[] rates = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    rates = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
                }

                if (recognitionResults == null || recognitionResults.isEmpty()) {
                    // empty response
                    GoogleRecognitionServiceImpl.this.onResult(new AIResponse());
                } else {
                    final AIRequest aiRequest = new AIRequest();
                    if (rates != null) {
                        aiRequest.setQuery(recognitionResults.toArray(new String[recognitionResults.size()]), rates);
                    } else {
                        aiRequest.setQuery(recognitionResults.get(0));
                    }

                    // notify listeners about the last recogntion result for more accurate user feedback
                    GoogleRecognitionServiceImpl.this.onPartialResults(recognitionResults);
                    GoogleRecognitionServiceImpl.this.sendRequest(aiRequest, requestExtras);
                }
            }

            recognitionActive = false;
        }

        @Override
        public void onPartialResults(final Bundle partialResults) {
            if (recognitionActive) {
                final ArrayList<String> partialRecognitionResults = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partialRecognitionResults != null && !partialRecognitionResults.isEmpty()) {
                    GoogleRecognitionServiceImpl.this.onPartialResults(partialRecognitionResults);
                }
            }
        }

        @Override
        public void onEvent(final int eventType, final Bundle params) {
        }
    }

}
