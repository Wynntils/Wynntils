/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.downloader;

import com.wynntils.core.WynntilsMod;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URLConnection;
import java.util.function.Consumer;

public class DownloadableResource {
    private final File localFile;

    public DownloadableResource(File localFile) {
        this.localFile = localFile;
    }

    public Reader waitAndGetReader() {
        return null;
    }

    public void onCompletion(Consumer<Reader> onCompletion, Consumer<Throwable> onError) {}

    public void onCompletion(Consumer<Reader> onCompletion) {
        onCompletion(onCompletion, onError -> {
            WynntilsMod.warn("Error while reading resource");
        });
    }

    public InputStream waitAndGetInputStream() {
        return null;
    }

    public long getTimestamp() {
        URLConnection st = null;
        return Long.parseLong(st.getHeaderField("timestamp"));
    }

    public void waitForCompletion() {
    }

    public void setTimeoutMs(int timeOutMs) {
    }

    public boolean isSuccessful() {
        return true;
    }
}
