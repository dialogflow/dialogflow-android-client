package com.speaktoit.ai.http;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class HttpClient {

    private HttpURLConnection con;
    private OutputStream os;

    private final String delimiter = "--";
    private final String boundary =  "SwA"+Long.toString(System.currentTimeMillis())+"SwA";

    public HttpClient(final HttpURLConnection con) {
        this.con = con;
    }

    public void connectForMultipart() throws IOException {
     //   con = (HttpURLConnection) ( new URL(url)).openConnection();
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        con.connect();
        os = con.getOutputStream();
    }

    public void addFormPart(final String paramName, final String value) throws IOException {
        os.write( (delimiter + boundary + "\r\n").getBytes());
        os.write( "Content-Type: text/plain\r\n".getBytes());
        os.write( ("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());
        os.write( ("\r\n" + value + "\r\n").getBytes());
    }

    public void addFilePart(final String paramName, final String fileName, final byte[] data) throws IOException {
        os.write( (delimiter + boundary + "\r\n").getBytes());
        os.write( ("Content-Disposition: form-data; name=\"" + paramName +  "\"; filename=\"" + fileName + "\"\r\n"  ).getBytes());
        os.write( ("Content-Type: application/octet-stream\r\n"  ).getBytes());
        os.write( ("Content-Transfer-Encoding: binary\r\n"  ).getBytes());
        os.write("\r\n".getBytes());

        os.write(data);

        os.write("\r\n".getBytes());
    }

    public void finishMultipart() throws IOException {
        os.write( (delimiter + boundary + delimiter + "\r\n").getBytes());
    }


    public String getResponse() throws IOException {
        final InputStream is = con.getInputStream();
        final byte[] b1 = new byte[1024];
        final StringBuilder buffer = new StringBuilder();

        while ( is.read(b1) != -1)
        {
            buffer.append(new String(b1));
        }

        con.disconnect();

        return buffer.toString();
    }

}
