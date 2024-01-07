/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemvault;

import com.wynntils.core.components.Service;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.services.itemvault.type.SavedItem;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemVaultService extends Service {
    private static final String DEFAULT_CATEGORY = "Uncategorized";

    @Persisted
    public final Storage<Set<SavedItem>> savedItems = new Storage<>(new HashSet<>());

    @Persisted
    public final Storage<Set<String>> categories = new Storage<>(new HashSet<>(List.of(DEFAULT_CATEGORY)));

    public ItemVaultService() {
        super(List.of());
    }

    public String getDefaultCategory() {
        return DEFAULT_CATEGORY;
    }

    public SavedItem getItem(String base64) {
        for (SavedItem savedItem : savedItems.get()) {
            if (savedItem.base64().equals(base64)) {
                return savedItem;
            }
        }

        return null;
    }
}
