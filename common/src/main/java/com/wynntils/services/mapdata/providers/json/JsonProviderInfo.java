/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

public record JsonProviderInfo(
        String providerId,
        JsonProviderType providerType,
        String providerFilename,
        String providerFilePath,
        String providerUrl) {
    public static JsonProviderInfo createBuiltin(String providerId, String providerFilename) {
        return new JsonProviderInfo(providerId, JsonProviderType.BUNDLED, providerFilename, null, null);
    }

    public static JsonProviderInfo createLocal(String providerId, String providerFilePath) {
        return new JsonProviderInfo(providerId, JsonProviderType.LOCAL, null, providerFilePath, null);
    }

    public static JsonProviderInfo createRemote(String providerId, String providerUrl) {
        return new JsonProviderInfo(providerId, JsonProviderType.REMOTE, null, null, providerUrl);
    }

    public String path() {
        return switch (providerType) {
            case BUNDLED -> "bundled / " + providerFilename;
            case LOCAL -> "local / " + providerFilePath;
            case REMOTE -> "remote / " + providerUrl;
        };
    }

    public enum JsonProviderType {
        BUNDLED,
        LOCAL,
        REMOTE
    }
}
