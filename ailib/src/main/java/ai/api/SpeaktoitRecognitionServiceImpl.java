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
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.util.VoiceActivityDetector;

import java.io.IOException;
import java.io.InputStream;

public class SpeaktoitRecognitionServiceImpl extends AIService {

    public static final String TAG = SpeaktoitRecognitionServiceImpl.class.getName();

    private static final int SAMPLE_RATE_IN_HZ = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mediaRecorder;

    private final AIDataService aiDataService;

    private volatile boolean isRecording = false;

    private final VoiceActivityDetector voiceActivityDetector = new VoiceActivityDetector(SAMPLE_RATE_IN_HZ);

    protected SpeaktoitRecognitionServiceImpl(final Context context, final AIConfiguration config) {
        super(config, context);

        initMediaRecorder();

        aiDataService = new AIDataService(context, config);
    }

    private void initMediaRecorder() {
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        mediaRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                minBufferSize);

        voiceActivityDetector.setSpeechListener(new VoiceActivityDetector.SpeechEventsListener() {
            @Override
            public void onSpeechBegin() {

            }

            @Override
            public void onSpeechEnd() {
                if (mediaRecorder != null) {
                    mediaRecorder.stop();
                }
            }
        });
    }

    @Override
    public void startListening() {
        voiceActivityDetector.reset();

        mediaRecorder.startRecording();
        isRecording = true;

        onListeningStarted();

        new RequestTask(new RecorderWrapper(mediaRecorder)).execute();
    }

    @Override
    public void stopListening() {
        if (isRecording) {
            mediaRecorder.stop();
            isRecording = false;

            onListeningFinished();
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

    @Override
    public void pause() {
        super.pause();
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    @Override
    public void resume() {
        super.resume();

        if (mediaRecorder == null) {
            initMediaRecorder();
        }
    }

    private class RecorderWrapper extends InputStream {

        private final AudioRecord audioRecord;

        private RecorderWrapper(final AudioRecord audioRecord) {
            this.audioRecord = audioRecord;
        }

        @Override
        public int read() throws IOException {
            final byte[] buffer = new byte[1];
            audioRecord.read(buffer,0,1);
            return buffer[0];
        }

        @Override
        public int read(final byte[] buffer, final int byteOffset, final int byteCount) throws IOException {
            Log.v(TAG, "RecorderWrapper: read");
            final int bytesRead = audioRecord.read(buffer, byteOffset, byteCount);
            voiceActivityDetector.processBuffer(buffer, bytesRead);
            return bytesRead;
        }
    }

    private class RequestTask extends AsyncTask<Void, Void, AIResponse> {

        private final RecorderWrapper recorderWrapper;
        private AIError aiError;

        private RequestTask(final RecorderWrapper recorderWrapper) {
            this.recorderWrapper = recorderWrapper;
        }

        @Override
        protected AIResponse doInBackground(final Void... params) {
            try {
                final AIResponse aiResponse = aiDataService.voiceRequest(recorderWrapper);
                return aiResponse;
            } catch (final AIServiceException e) {
                aiError = new AIError("Wrong answer from server " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(final AIResponse aiResponse) {
            super.onPostExecute(aiResponse);

            if (aiResponse != null) {
                onResult(aiResponse);
            } else {
                onError(aiError);
            }
        }
    }

}
