/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import java.io.File;
import java.util.function.BiConsumer;

public final class JsonProviderInfo {
    private final String providerId;
    private final JsonProviderType providerType;

    // Builtin
    private final String providerFilename;

    // Local
    private final String providerFilePath;

    // Remote
    private final String providerUrl;

    private JsonProviderInfo(
            String providerId,
            JsonProviderType providerType,
            String providerFilename,
            String providerFilePath,
            String providerUrl) {
        this.providerId = providerId;
        this.providerType = providerType;
        this.providerFilename = providerFilename;
        this.providerFilePath = providerFilePath;
        this.providerUrl = providerUrl;
    }

    public static JsonProviderInfo createBuiltin(String providerId, String providerFilename) {
        return new JsonProviderInfo(providerId, JsonProviderType.BUILTIN, providerFilename, null, null);
    }

    public static JsonProviderInfo createLocal(String providerId, String providerFilePath) {
        return new JsonProviderInfo(providerId, JsonProviderType.LOCAL, null, providerFilePath, null);
    }

    public static JsonProviderInfo createRemote(String providerId, String providerUrl) {
        return new JsonProviderInfo(providerId, JsonProviderType.REMOTE, null, null, providerUrl);
    }

    public void load(BiConsumer<String, JsonProvider> loadedCallback) {
        switch (providerType) {
            case BUILTIN:
                loadedCallback.accept(providerId, JsonProvider.loadBundledResource(providerId, providerFilename));
                break;
            case LOCAL:
                loadedCallback.accept(providerId, JsonProvider.loadLocalFile(providerId, new File(providerFilePath)));
                break;
            case REMOTE:
                JsonProvider.loadOnlineResource(providerId, providerUrl, loadedCallback);
                break;
        }
    }

    public enum JsonProviderType {
        BUILTIN,
        LOCAL,
        REMOTE
    }
}
