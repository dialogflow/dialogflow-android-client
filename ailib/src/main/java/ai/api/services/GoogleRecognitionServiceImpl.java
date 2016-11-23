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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.AIServiceException;
import ai.api.PartialResultsListener;
import ai.api.RequestExtras;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.util.RecognizerChecker;
import ai.api.util.VersionConfig;

public class GoogleRecognitionServiceImpl extends AIService {

    private static final String TAG = GoogleRecognitionServiceImpl.class.getName();
    private static final long STOP_DELAY = 1000;

    private SpeechRecognizer speechRecognizer;
    private final Object speechRecognizerLock = new Object();
    private RequestExtras requestExtras;
    private PartialResultsListener partialResultsListener;
    private final VersionConfig versionConfig;

    private volatile boolean recognitionActive = false;
    private volatile boolean wasReadyForSpeech;

    private final Handler handler = new Handler();
    private Runnable stopRunnable;

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

        versionConfig = VersionConfig.init(context);
        if (versionConfig.isAutoStopRecognizer()) {
            stopRunnable = new Runnable() {
                @Override
                public void run() {
                    stopListening();
                }
            };
        }
    }

    /**
     * Manage recognizer cancellation runnable.
     *
     * @param action (int) (0 - stop, 1 - restart)
     */
    private void updateStopRunnable(final int action) {
        if (stopRunnable != null) {
            if (action == 0) {
                handler.removeCallbacks(stopRunnable);
            } else if (action == 1) {
                handler.removeCallbacks(stopRunnable);
                handler.postDelayed(stopRunnable, STOP_DELAY);
            }
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
        Log.d(TAG, "clearRecognizer");
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

                if (!checkPermissions()) {
                    final AIError aiError = new AIError("RECORD_AUDIO permission is denied. Please request permission from user.");
                    onError(aiError);
                    return;
                }

                initializeRecognizer();

                recognitionActive = true;

                final Intent sttIntent = createRecognitionIntent();

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

    private Intent createRecognitionIntent() {
        final Intent sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        final String language = config.getLanguage().replace('-', '_');

        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
        sttIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        sttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());

        // WORKAROUND for https://code.google.com/p/android/issues/detail?id=75347
        sttIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{language});
        return sttIntent;
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

    private void restartRecognition() {
        updateStopRunnable(0);
        recognitionActive = false;

        synchronized (speechRecognizerLock) {
            try {
                if (speechRecognizer != null) {
                    speechRecognizer.cancel();

                    final Intent intent = createRecognitionIntent();
                    wasReadyForSpeech = false;
                    speechRecognizer.startListening(intent);
                    recognitionActive = true;
                }
            } catch (Exception e) {
                stopListening();
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

    private void stopInternal() {
        updateStopRunnable(0);
        if (versionConfig.isDestroyRecognizer()) clearRecognizer();
        recognitionActive = false;
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
                Log.d(TAG, "SpeechRecognizer.ERROR_NO_MATCH, restartRecognition()");
                restartRecognition();
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
            stopInternal();
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
            stopInternal();
        }

        @Override
        public void onPartialResults(final Bundle partialResults) {
            if (recognitionActive) {
                updateStopRunnable(1);
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
