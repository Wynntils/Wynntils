/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.CoreComponent;
import java.io.Reader;
import java.util.Objects;
import java.util.function.Consumer;

public class QueuedDownload {
    private static final Consumer<Throwable> DEFAULT_ERROR_HANDLER =
            (exception) -> WynntilsMod.error("Error while processing download request; ignored");

    private final CoreComponent callerComponent;
    private final UrlId urlId;
    private final Dependency dependency;

    // Callbacks for handling the download result,
    // which are provided to download when it is processed
    private Consumer<Reader> onCompletionReader;
    private Consumer<JsonObject> onCompletionJsonObject;
    private Consumer<JsonArray> onCompletionJsonArray;

    QueuedDownload(CoreComponent callerComponent, UrlId urlId, Dependency dependency) {
        this.callerComponent = callerComponent;
        this.urlId = urlId;
        this.dependency = dependency;
    }

    public void handleReader(Consumer<Reader> readerConsume) {
        this.onCompletionReader = readerConsume;
        this.onCompletionJsonObject = null;
        this.onCompletionJsonArray = null;
    }

    public void handleJsonObject(Consumer<JsonObject> jsonObjectConsume) {
        this.onCompletionJsonObject = jsonObjectConsume;
        this.onCompletionReader = null;
        this.onCompletionJsonArray = null;
    }

    public void handleJsonArray(Consumer<JsonArray> jsonArrayConsume) {
        this.onCompletionJsonArray = jsonArrayConsume;
        this.onCompletionReader = null;
        this.onCompletionJsonObject = null;
    }

    public CoreComponent callerComponent() {
        return callerComponent;
    }

    public UrlId urlId() {
        return urlId;
    }

    public Dependency dependency() {
        return dependency;
    }

    public Consumer<Reader> onCompletionReader() {
        return onCompletionReader;
    }

    public Consumer<JsonObject> onCompletionJsonObject() {
        return onCompletionJsonObject;
    }

    public Consumer<JsonArray> onCompletionJsonArray() {
        return onCompletionJsonArray;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueuedDownload that = (QueuedDownload) o;
        return Objects.equals(callerComponent, that.callerComponent)
                && urlId == that.urlId
                && Objects.equals(dependency, that.dependency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callerComponent, urlId, dependency);
    }

    @Override
    public String toString() {
        return "QueuedDownload{" + "callerComponent="
                + callerComponent + ", urlId="
                + urlId + ", dependency="
                + dependency + '}';
    }
}
