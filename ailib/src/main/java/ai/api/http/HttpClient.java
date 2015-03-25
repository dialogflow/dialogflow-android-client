package ai.api.http;

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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class HttpClient {

    public static final String TAG = HttpClient.class.getName();
    private static final int CHUNK_LENGTH = 2048;

    @NonNull
    private final HttpURLConnection connection;
    private OutputStream os;

    private final String delimiter = "--";
    private final String boundary = "SwA" + Long.toString(System.currentTimeMillis()) + "SwA";

    private boolean writeSoundLog;

    public HttpClient(@NonNull final HttpURLConnection connection) {
        this.connection = connection;
    }

    public void connectForMultipart() throws IOException {
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setChunkedStreamingMode(CHUNK_LENGTH);
        connection.connect();
        os = connection.getOutputStream();
    }

    public void addFormPart(@NonNull final String paramName, @NonNull final String value) throws IOException {
        os.write((delimiter + boundary + "\r\n").getBytes());
        os.write("Content-Type: application/json\r\n".getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());
        os.write(("\r\n" + value + "\r\n").getBytes());
    }

    public void addFilePart(@NonNull final String paramName, @NonNull final String fileName, @NonNull final InputStream data) throws IOException {
        os.write((delimiter + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
        os.write(("Content-Type: audio/wav\r\n").getBytes());
        //os.write( ("Content-Transfer-Encoding: binary\r\n"  ).getBytes());
        os.write("\r\n".getBytes());

        Log.v(TAG, "Sound write start");

        FileOutputStream outputStream = null;

        if (writeSoundLog) {
            final File cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "sound_log");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            Log.d(TAG, cacheDir.getAbsolutePath());

            final File soundFile = new File(cacheDir, "log.wav");
            outputStream = new FileOutputStream(soundFile, false);
        }

        //TODO remove magic number
        final int bufferSize = 4096;
        final byte[] buffer = new byte[bufferSize];

        int bytesActuallyRead;

        bytesActuallyRead = data.read(buffer, 0, bufferSize);
        Log.v(TAG, "Bytes read: " + bytesActuallyRead);

        while (bytesActuallyRead >= 0) {
            if (bytesActuallyRead > 0) {
                os.write(buffer, 0, bytesActuallyRead);

                if (writeSoundLog) {
                    outputStream.write(buffer, 0, bytesActuallyRead);
                }
            }
            bytesActuallyRead = data.read(buffer, 0, bufferSize);
            Log.v(TAG, "Bytes read: " + bytesActuallyRead);
        }

        if (writeSoundLog) {
            outputStream.close();
        }

        Log.v(TAG, "Sound write finished");

        os.write("\r\n".getBytes());
    }

    public void finishMultipart() throws IOException {
        os.write((delimiter + boundary + delimiter + "\r\n").getBytes());
        os.close();
    }

    @NonNull
    public String getResponse() throws IOException {
        final InputStream inputStream = new BufferedInputStream(connection.getInputStream());
        final String response = IOUtils.toString(inputStream, Charsets.UTF_8);
        inputStream.close();
        return response;
    }

    @Nullable
    public String getErrorString() {
        try {
            final InputStream inputStream = new BufferedInputStream(connection.getErrorStream());
            final String response;
            response = IOUtils.toString(inputStream, Charsets.UTF_8);
            inputStream.close();
            return response;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setWriteSoundLog(final boolean writeSoundLog) {
        this.writeSoundLog = writeSoundLog;
    }
}
