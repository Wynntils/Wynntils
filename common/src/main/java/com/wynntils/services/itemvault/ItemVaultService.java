/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemvault;

import com.wynntils.core.components.Service;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.services.itemvault.type.SavedItemStack;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ItemVaultService extends Service {
    private static final String DEFAULT_CATEGORY = "Uncategorized";

    @Persisted
    public final Storage<Map<String, Map<String, SavedItemStack>>> savedItems = new Storage<>(new TreeMap<>());

    public ItemVaultService() {
        super(List.of());

        savedItems.get().putIfAbsent("Uncategorized", new TreeMap<>());
    }

    public String getDefaultCategory() {
        return DEFAULT_CATEGORY;
    }
}