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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GoogleRecognitionServiceImpl extends AIService {

    private static final String TAG = GoogleRecognitionServiceImpl.class.getName();

    private final SpeechRecognizer speechRecognizer;
    private volatile boolean recognitionActive = false;
    private final AIDataService aiDataService;

    private final Map<Integer, String> errorMessages = new HashMap<>();

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

    protected GoogleRecognitionServiceImpl(final Context context, final AIConfiguration config) {
        super(config);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new InternalRecognitionListener());

        aiDataService = new AIDataService(config);
    }

    /**
     * This method must be called from UI Thread
     *
     * @param aiRequest
     */
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
        if (!recognitionActive) {
            final Intent sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, config.getLanguage());
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, config.getLanguage());
            speechRecognizer.startListening(sttIntent);
            recognitionActive = true;
        } else {
            Log.w(TAG, "Trying to start recognition while another recognition active");
        }

    }

    @Override
    public void stopListening() {
        if (recognitionActive) {
            speechRecognizer.stopListening();
            recognitionActive = false;
        } else {
            Log.w(TAG, "Trying to stop listening while not active recognition");
        }
    }

    @Override
    public void cancel() {
        if (recognitionActive) {
            speechRecognizer.cancel();
            recognitionActive = false;
        }
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
                    GoogleRecognitionServiceImpl.this.sendRequest(aiRequest);
                }
            }
        }

        @Override
        public void onPartialResults(final Bundle partialResults) {

        }

        @Override
        public void onEvent(final int eventType, final Bundle params) {

        }
    }


}
