/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.storage;

import com.google.gson.JsonObject;
import com.wynntils.core.net.DownloadSource;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import com.wynntils.utils.EnumUtils;
import java.util.Set;

public class DownloadSourceStringToEnumUpfixer implements Upfixer {
    private static final String CUSTOM_SOURCE_KEY_OLD = "manager.url.customSourceurl";
    private static final String CUSTOM_SOURCE_KEY_NEW = "manager.url.customSourceUrl";
    private static final String DOWNLOAD_SOURCE_KEY = "manager.url.downloadSourceUrl";

    // Purposely missing the branch in case it was set to the beta branch
    private static final String GITHUB_VALUE = "https://raw.githubusercontent.com/Wynntils/Static-Storage/refs/heads/";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        String customSource = "";

        if (configObject.has(CUSTOM_SOURCE_KEY_OLD)) {
            customSource = configObject.get(CUSTOM_SOURCE_KEY_OLD).getAsString();

            configObject.remove(CUSTOM_SOURCE_KEY_OLD);
            configObject.addProperty(CUSTOM_SOURCE_KEY_NEW, customSource);
        }

        if (configObject.has(DOWNLOAD_SOURCE_KEY)) {
            String downloadSource = configObject.get(DOWNLOAD_SOURCE_KEY).getAsString();

            if (downloadSource.startsWith(GITHUB_VALUE)) {
                configObject.addProperty(DOWNLOAD_SOURCE_KEY, EnumUtils.toJsonFormat(DownloadSource.GITHUB));
            } else if (!customSource.isEmpty()) {
                configObject.addProperty(DOWNLOAD_SOURCE_KEY, EnumUtils.toJsonFormat(DownloadSource.CUSTOM));
            } else {
                // Default to cdn
                configObject.addProperty(DOWNLOAD_SOURCE_KEY, EnumUtils.toJsonFormat(DownloadSource.CDN));
            }
        }
        return true;
    }
}
