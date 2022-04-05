/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.request;

import com.google.gson.JsonElement;
import com.wynntils.core.webapi.request.multipart.IMultipartFormPart;
import com.wynntils.utils.objects.ThrowingConsumer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PostRequestBuilder extends RequestBuilder {

    private ThrowingConsumer<HttpURLConnection, IOException> writer;

    public PostRequestBuilder(String url, String id) {
        super(url, id);
    }

    /**
     * Set a consumer that will write to a HttpURLConnection
     *
     * @return
     */
    public PostRequestBuilder setWriter(ThrowingConsumer<HttpURLConnection, IOException> writer) {
        this.writer = writer;
        return this;
    }

    /** Sets the writer to one that just writes the given bytes */
    public PostRequestBuilder postBytes(byte[] data, String contentType) {
        return setWriter(conn -> {
            conn.addRequestProperty("Content-Type", contentType);
            conn.addRequestProperty("Content-Length", Integer.toString(data.length));
            OutputStream o = conn.getOutputStream();
            o.write(data);
            o.flush();
        });
    }

    /** Sets the write to a json string from a json element */
    public PostRequestBuilder postJsonElement(JsonElement element) {
        return postBytes(element.toString().getBytes(), "application/json");
    }

    private static final byte[] newline = "\r\n".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] multipartEnd = "--\r\n".getBytes(StandardCharsets.US_ASCII);

    /** Sets the writer to one that writes multipart/form-data. */
    public PostRequestBuilder postMultipart(Iterable<? extends IMultipartFormPart> parts) {
        return setWriter(conn -> {
            String boundary = "----" + UUID.randomUUID().toString();
            conn.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            boundary = "--" + boundary;
            byte[] boundaryBytes = ("\r\n" + boundary).getBytes(StandardCharsets.US_ASCII);
            int length = 0;
            int count = 0;
            for (IMultipartFormPart f : parts) {
                length += f.getLength();
                ++count;
            }

            int totalLength =
                    // Boundary above and below every file (Except -4 the first and last
                    // \r\n, and +2 for "--" at the end)
                    (boundaryBytes.length + 2) * (count + 1) - 2 + length;
            conn.addRequestProperty("Content-Length", Integer.toString(totalLength));

            OutputStream o = conn.getOutputStream();
            o.write(boundary.getBytes(StandardCharsets.US_ASCII));
            for (IMultipartFormPart f : parts) {
                o.write(newline);
                f.write(o);
                o.write(boundaryBytes);
            }
            o.write(multipartEnd);
            o.flush();
        });
    }

    /**
     * After setting a writer with {{@link #setWriter(ThrowingConsumer)}} or similar, the consumer
     * supplied here will be called first.
     *
     * <p>For example, this can be used to set headers.
     */
    public PostRequestBuilder setBefore(ThrowingConsumer<HttpURLConnection, IOException> writer) {
        ThrowingConsumer<HttpURLConnection, IOException> previous = this.writer;
        this.writer = conn -> {
            writer.accept(conn);
            previous.accept(conn);
        };
        return this;
    }

    public PostRequest build() {
        return new PostRequest(
                this.url,
                this.id,
                this.parallelGroup,
                this.handler,
                this.useCacheAsBackup,
                this.onError,
                this.headers,
                this.cacheFile,
                this.cacheValidator,
                this.timeout,
                writer);
    }
}
