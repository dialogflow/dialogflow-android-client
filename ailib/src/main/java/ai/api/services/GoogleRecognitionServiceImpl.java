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
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.AIConfiguration;
import ai.api.AIService;
import ai.api.AIServiceException;
import ai.api.PartialResultsListener;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.util.RecognizerChecker;

public class GoogleRecognitionServiceImpl extends AIService {

    private static final String TAG = GoogleRecognitionServiceImpl.class.getName();

    private SpeechRecognizer speechRecognizer;
    private final Object speechRecognizerLock = new Object();

    private volatile boolean recognitionActive = false;

    private final Handler handler;

    private final Map<Integer, String> errorMessages = new HashMap<Integer, String>();

    {
        errorMessages.put(1, "Network operation timed out.");
        errorMessages.put(2, "Other network related errors.");
        errorMessages.put(3, "Audio recording error.");
        errorMessages.put(4, "Server sends error status.");
        errorMessages.put(5, "Other client side errors.");
        errorMessages.put(6, "No speech input.");
        errorMessages.put(7, "No recognition result matched.");
        errorMessages.put(8, "RecognitionService busy.");
        errorMessages.put(9, "Insufficient permissions.");
    }

    private List<AIContext> contexts;
    private PartialResultsListener partialResultsListener;

    public GoogleRecognitionServiceImpl(final Context context, final AIConfiguration config) {
        super(config, context);

        final ComponentName googleRecognizerComponent = RecognizerChecker.findGoogleRecognizer(context);
        if (googleRecognizerComponent == null) {
            Log.w(TAG, "Google Recognizer application not found on device. Quality of the recognition may be low. Please check if Google Search application installed and enabled.");
        }

        handler = new Handler(context.getMainLooper());
    }

    protected void initializeRecognizer() {
        synchronized (speechRecognizerLock) {
            if (speechRecognizer != null) {
                speechRecognizer.destroy();
                speechRecognizer = null;
            }

            final ComponentName googleRecognizerComponent = RecognizerChecker.findGoogleRecognizer(context);

            if (googleRecognizerComponent == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            } else {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context, googleRecognizerComponent);
            }

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

    private void sendRequest(final AIRequest aiRequest) {

        if (aiRequest == null) {
            throw new IllegalArgumentException("aiRequest must be not null");
        }

        final AsyncTask<AIRequest, Integer, AIResponse> task = new AsyncTask<AIRequest, Integer, AIResponse>() {

            private AIError aiError;

            @Override
            protected AIResponse doInBackground(final AIRequest... params) {
                final AIRequest request = params[0];
                try {
                    final AIResponse response = aiDataService.request(request);
                    return response;
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
        startListening(null);
    }

    @Override
    public void startListening(final List<AIContext> contexts) {
        if (!recognitionActive) {
            this.contexts = contexts;

            final Intent sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            final String language = config.getLanguage().replace('-','_');

            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
            sttIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

            // WORKAROUND for https://code.google.com/p/android/issues/detail?id=75347
            // TODO Must be removed after fix in Android
            sttIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{});

            runInUiThread(new Runnable() {
                @Override
                public void run() {
                    initializeRecognizer();

                    speechRecognizer.startListening(sttIntent);
                    recognitionActive = true;
                }
            });

        } else {
            Log.w(TAG, "Trying to start recognition while another recognition active");
        }
    }

    @Override
    public void stopListening() {
        if (recognitionActive) {
            runInUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (speechRecognizerLock) {
                        if (recognitionActive) {
                            speechRecognizer.stopListening();
                            recognitionActive = false;
                        }
                    }
                }
            });
        } else {
            Log.w(TAG, "Trying to stop listening while not active recognition");
        }
    }

    @Override
    public void cancel() {
        if (recognitionActive) {
            runInUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (speechRecognizerLock) {
                        if (recognitionActive) {
                            speechRecognizer.cancel();
                            recognitionActive = false;
                        }
                    }
                }
            });

        }
    }

    /**
     * This method must be called from UI thread
     */
    @Override
    public void pause() {
        super.pause();

        clearRecognizer();
    }

    /**
     * This method must be called from UI thread
     */
    @Override
    public void resume() {
        super.resume();
    }

    public void setPartialResultsListener(PartialResultsListener partialResultsListener) {
        this.partialResultsListener = partialResultsListener;
    }

    protected void onPartialResults(final List<String> partialResults) {
        if (partialResultsListener != null) {
            partialResultsListener.onPartialResults(partialResults);
        }
    }

    private void runInUiThread(final Runnable runnable) {
        handler.post(runnable);
    }

    private class InternalRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(final Bundle params) {
            GoogleRecognitionServiceImpl.this.onListeningStarted();
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(final float rmsdB) {
            GoogleRecognitionServiceImpl.this.onAudioLevelChanged(rmsdB);
        }

        @Override
        public void onBufferReceived(final byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            GoogleRecognitionServiceImpl.this.onListeningFinished();
        }

        @Override
        public void onError(final int error) {
            recognitionActive = false;

            final AIError aiError;

            if (errorMessages.containsKey(error)) {
                final String description = errorMessages.get(error);
                aiError = new AIError("Speech recognition engine error: " + description);
            } else {
                aiError = new AIError("Speech recognition engine error: " + error);
            }
            GoogleRecognitionServiceImpl.this.onError(aiError);
        }

        @TargetApi(14)
        @Override
        public void onResults(final Bundle results) {
            if (recognitionActive) {
                recognitionActive = false;

                final ArrayList<String> recognitionResults = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                float[] rates = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    rates = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
                }

                if (recognitionResults == null || recognitionResults.size() == 0) {
                    // empty response
                    GoogleRecognitionServiceImpl.this.onResult(new AIResponse());
                } else {
                    final AIRequest aiRequest = new AIRequest();
                    if (rates != null) {
                        aiRequest.setQuery(recognitionResults.toArray(new String[recognitionResults.size()]), rates);
                    } else {
                        aiRequest.setQuery(recognitionResults.get(0));
                    }

                    if (contexts != null) {
                        aiRequest.setContexts(contexts);
                    }

                    GoogleRecognitionServiceImpl.this.sendRequest(aiRequest);

                    clearRecognizer();
                }
            }
        }

        @Override
        public void onPartialResults(final Bundle partialResults) {
            final ArrayList<String> partialRecognitionResults = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (partialRecognitionResults != null && !partialRecognitionResults.isEmpty()) {
                GoogleRecognitionServiceImpl.this.onPartialResults(partialRecognitionResults);
            }
        }

        @Override
        public void onEvent(final int eventType, final Bundle params) {

        }
    }

}
