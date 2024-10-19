/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.google.common.base.CaseFormat;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.persisted.storage.Storageable;
import java.util.Locale;

public abstract class CoreComponent implements Storageable {
    @Override
    public String getStorageJsonName() {
        String name = this.getClass().getSimpleName().replace(getTypeName(), "");
        String nameCamelCase = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
        return getTypeName().toLowerCase(Locale.ROOT) + "." + nameCamelCase;
    }

    public void registerDownloads(DownloadRegistry registry) {
        // Override this method to register downloads for this component.
    }

    // DO NOT call this method directly, especially not from a CoreComponent constructor.
    public void reloadData() {
        // This method is used for a hook for loading and reloading data for core components. Mainly, this method is
        // used for downloading dynamic data using UrlIds, which is not available during a component constructor.
        // This means that this method is called once UrlManager finished loading the URL lists,
        // and decided how to merge them.
    }
}
