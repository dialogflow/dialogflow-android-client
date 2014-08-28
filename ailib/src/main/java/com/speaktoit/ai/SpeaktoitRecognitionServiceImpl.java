package com.speaktoit.ai;

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
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.speaktoit.ai.model.AIError;
import com.speaktoit.ai.model.AIResponse;
import com.speaktoit.ai.model.QuestionMetadata;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class SpeaktoitRecognitionServiceImpl extends AIService {

    public static final String TAG = SpeaktoitRecognitionServiceImpl.class.getName();

    private static final int SAMPLE_RATE_IN_HZ = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private final AudioRecord mediaRecorder;

    private volatile boolean isRecording = false;

    private ByteArrayOutputStream outputStream;

    protected SpeaktoitRecognitionServiceImpl(final Context context, final AIConfiguration config) {
        super(config);

        mediaRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT));
    }

    @Override
    public void startListening() {
        outputStream = new ByteArrayOutputStream();

        mediaRecorder.startRecording();
        isRecording = true;

        onListeningStarted();

    }

    @Override
    public void stopListening() {
        if (isRecording) {
            mediaRecorder.stop();
            isRecording = false;

            onListeningFinished();

            sendRequest(outputStream.toByteArray());
        }

    }

    @Override
    public void cancel() {
        if (isRecording) {
            mediaRecorder.stop();
            isRecording = false;

            onListeningFinished();
        }
    }

    private void sendRequest(final byte[] soundData) {
        try {
            final URL url = new URL(config.getQuestionUrl());

            final QuestionMetadata questionMetadata = new QuestionMetadata();
            questionMetadata.setLanguage(config.getLanguage());
            questionMetadata.setAgentId(config.getAgentId());
            questionMetadata.setTimezone(Calendar.getInstance().getTimeZone().getID());

            final SpeaktoitRecognitionRequest speaktoitRecognitionRequest = new SpeaktoitRecognitionRequest();
            speaktoitRecognitionRequest.setMetadata(questionMetadata);
            speaktoitRecognitionRequest.setSoundData(soundData);

            final SpeaktoitRecognitionRequestTask requestTask = new SpeaktoitRecognitionRequestTask(url){
                @Override
                protected void onPostExecute(final String stringResult) {
                    try {
                        final AIResponse aiResponse = GsonFactory.getGson().fromJson(stringResult, AIResponse.class);
                        onResult(aiResponse);

                    } catch (final Exception e) {
                        final AIError aiError = new AIError("Wrong answer from server " + e.toString());
                        onError(aiError);
                    }
                }
            };

            requestTask.execute(speaktoitRecognitionRequest);

        }catch (final MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
