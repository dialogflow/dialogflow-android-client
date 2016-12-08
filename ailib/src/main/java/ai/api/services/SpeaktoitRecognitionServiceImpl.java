package ai.api.services;

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
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.util.VoiceActivityDetector;

import static ai.api.util.VoiceActivityDetector.FRAME_SIZE_IN_BYTES;
import static ai.api.util.VoiceActivityDetector.NOISE_BYTES;

/*
* @deprecated Use GoogleRecognitionServiceImpl
*/
@Deprecated
public class SpeaktoitRecognitionServiceImpl extends AIService implements
        VoiceActivityDetector.SpeechEventsListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {

    public static final String TAG = SpeaktoitRecognitionServiceImpl.class.getName();

    private static final int SAMPLE_RATE_IN_HZ = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private final ExecutorService eventsExecutor = Executors.newSingleThreadExecutor();
    private final VoiceActivityDetector vad = new VoiceActivityDetector(SAMPLE_RATE_IN_HZ);


    private AudioRecord audioRecord;

    private final Object recognizerLock = new Object();
    private volatile boolean isRecording = false;

    private MediaPlayer mediaPlayer;

    private RequestExtras extras;
    private RecognizeTask recognizeTask;

    public SpeaktoitRecognitionServiceImpl(final Context context, final AIConfiguration config) {
        super(config, context);
        init();
    }

    private void init() {
        synchronized (recognizerLock) {
            final int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize);

            vad.setEnabled(config.isVoiceActivityDetectionEnabled());
            vad.setSpeechListener(this);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnCompletionListener(this);
        }
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
        synchronized (recognizerLock) {
            if (!isRecording) {

                if (!checkPermissions()) {
                    final AIError aiError = new AIError("RECORD_AUDIO permission is denied. Please request permission from user.");
                    onError(aiError);
                    return;
                }

                isRecording = true;
                extras = requestExtras;

                final AssetFileDescriptor startSound = config.getRecognizerStartSound();
                if (startSound != null) {
                    final boolean success = playSound(startSound);
                    if (!success) {
                        startRecording(extras);
                    }
                } else {
                    startRecording(extras);
                }
            } else {
                Log.w(TAG, "Trying start listening when it already active");
            }
        }
    }

    private void startRecording(final RequestExtras extras) {
        vad.reset();

        audioRecord.startRecording();

        onListeningStarted();

        recognizeTask = new RecognizeTask(new RecorderStream(audioRecord), extras);
        recognizeTask.execute();
    }

    @Override
    public void stopListening() {
        synchronized (recognizerLock) {
            if (isRecording) {
                try {
                    audioRecord.stop();
                    isRecording = false;

                    final AssetFileDescriptor stopSound = config.getRecognizerStopSound();
                    if (stopSound != null) {
                        playSound(stopSound);
                    }

                    onListeningFinished();

                } catch (final IllegalStateException e) {
                    Log.w(TAG, "Attempt to stop audioRecord when it is stopped");
                }
            }
        }
    }

    @Override
    public void cancel() {
        synchronized (recognizerLock) {
            if (isRecording) {
                audioRecord.stop();
                isRecording = false;

                final AssetFileDescriptor cancelSound = config.getRecognizerCancelSound();
                if (cancelSound != null) {
                    playSound(cancelSound);
                }
            }
            if (recognizeTask != null) {
                recognizeTask.cancel(true);
            }
            onListeningCancelled();
        }
    }

    @Override
    public void pause() {
        synchronized (recognizerLock) {
            if (isRecording) {
                audioRecord.stop();
                isRecording = false;
            }
            audioRecord.release();
            audioRecord = null;

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void resume() {
        init();
    }

    private boolean playSound(AssetFileDescriptor afd) {
        boolean result = true;
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    @Override
    public void onSpeechBegin() {
    }

    @Override
    public void onSpeechEnd() {
        eventsExecutor.submit(new Runnable() {
            @Override
            public void run() {
                stopListening();
            }
        });
    }

    @Override
    public void onSpeechCancel() {
        eventsExecutor.submit(new Runnable() {
            @Override
            public void run() {
                cancel();
            }
        });
    }

    private class RecorderStream extends InputStream {

        @SuppressWarnings("MagicNumber")
        private final float dbLevel = (float) Math.pow(10.0, -1.0 / 20.0);

        private final AudioRecord audioRecord;

        private byte[] bytes;
        private final Object bytesLock = new Object();

        int offset = 0;
        int max = 0;
        int min = 0;
        float alignment = 0;
        float count = 1;
        int extent;

        private RecorderStream(final AudioRecord audioRecord) {
            this.audioRecord = audioRecord;
        }

        @Override
        public int read() throws IOException {
            final byte[] buffer = new byte[1];
            audioRecord.read(buffer, 0, 1);
            return buffer[0];
        }

        @Override
        public int read(@NonNull final byte[] buffer, final int byteOffset, final int byteCount) throws IOException {
            final int bytesRead = audioRecord.read(buffer, byteOffset, byteCount);
            if (bytesRead > 0) {
                synchronized (bytesLock) {
                    if (config.isNormalizeInputSound())
                        normalize(buffer, bytesRead);

                    byte[] temp = bytes;
                    int tempLength = temp != null ? temp.length : 0;
                    bytes = new byte[tempLength + bytesRead];
                    if (tempLength > 0) {
                        System.arraycopy(temp, 0, bytes, 0, tempLength);
                    }
                    System.arraycopy(buffer, 0, bytes, tempLength, bytesRead);

                    while (bytes.length >= FRAME_SIZE_IN_BYTES) {
                        final byte[] b = new byte[FRAME_SIZE_IN_BYTES];
                        System.arraycopy(bytes, 0, b, 0, FRAME_SIZE_IN_BYTES);
                        vad.processBuffer(b, FRAME_SIZE_IN_BYTES);

                        temp = bytes;
                        final int newLength = temp.length - FRAME_SIZE_IN_BYTES;
                        bytes = new byte[newLength];
                        System.arraycopy(temp, FRAME_SIZE_IN_BYTES, bytes, 0, newLength);
                    }
                    onAudioLevelChanged((float) vad.calculateRms());
                }
            }
            return bytesRead != 0 ? bytesRead : AudioRecord.ERROR_INVALID_OPERATION;
        }

        private void normalize(@NonNull final byte[] buffer, final int bytesRead) {
            final int remainOffset = NOISE_BYTES - offset;
            if (bytesRead >= remainOffset) {
                final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, remainOffset, bytesRead - remainOffset).order(ByteOrder.LITTLE_ENDIAN);
                final ShortBuffer shorts = byteBuffer.asShortBuffer();
                for (int i = 0; i < shorts.limit(); i++) {
                    final short sample = shorts.get(i);
                    max = Math.max(max, sample);
                    min = Math.min(min, sample);
                    alignment = (count - 1) / count * alignment + sample / count;
                    count++;
                }
                extent = Math.max(Math.abs(max), Math.abs(min));
                final float factor = dbLevel * Short.MAX_VALUE / extent;
                for (int i = 0; i < shorts.limit(); i++) {
                    byteBuffer.putShort((short) ((shorts.get(i) - alignment) * factor));
                }
            }
            offset += Math.min(bytesRead, remainOffset);
        }
    }

    private class RecognizeTask extends AsyncTask<Void, Void, AIResponse> {

        private final RecorderStream recorderStream;
        private final RequestExtras requestExtras;

        private AIError aiError;

        private RecognizeTask(final RecorderStream recorderStream, final RequestExtras requestExtras) {
            this.recorderStream = recorderStream;
            this.requestExtras = requestExtras;
        }

        @Override
        protected AIResponse doInBackground(final Void... params) {
            try {
                return aiDataService.voiceRequest(recorderStream, requestExtras);
            } catch (final AIServiceException e) {
                aiError = new AIError(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final AIResponse aiResponse) {
            if (isCancelled()) {
                return;
            }
            if (aiResponse != null) {
                onResult(aiResponse);
            } else {
                SpeaktoitRecognitionServiceImpl.this.cancel();
                onError(aiError);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isRecording) {
            startRecording(extras);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (isRecording) {
            startRecording(extras);
        }
        return false;
    }

}
