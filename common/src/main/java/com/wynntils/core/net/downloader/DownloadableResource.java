/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.downloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.function.Predicate;

public class DownloadableResource {
    private final File localFile;

    public DownloadableResource(File localFile) {
        this.localFile = localFile;
    }

    public void handleJsonObject(Predicate<JsonObject> handler) {}

    public void handleBytes(Predicate<byte[]> handler) {}

    public void handleJsonArray(Predicate<JsonArray> handler) {
    }
}
