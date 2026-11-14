/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import com.wynntils.services.favorites.type.FavoriteCategory;
import com.wynntils.services.favorites.type.LootFilter;
import java.util.Set;

public class ItemFavoritesToLootFiltersUpfixer implements Upfixer {
    private static final String OLD_FAVORITE_ITEMS_KEY = "itemFavoriteFeature.favoriteItems";
    private static final String OLD_CLOSE_OVERRIDE_KEY = "itemFavoriteFeature.lootChestCloseOverride";
    private static final String LOOT_FILTERS_KEY = "itemFavoriteFeature.lootFilters";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        JsonElement favoriteItems = configObject.remove(OLD_FAVORITE_ITEMS_KEY);
        JsonElement closeOverride = configObject.remove(OLD_CLOSE_OVERRIDE_KEY);
        if (favoriteItems == null && closeOverride == null) return false;

        JsonObject category = new JsonObject();
        category.addProperty("name", FavoriteCategory.DEFAULT_NAME);
        if (favoriteItems != null) {
            category.add("items", favoriteItems);
        }
        if (closeOverride != null) {
            // 0 used to mean that the chest can never be closed
            int attempts = closeOverride.getAsInt();
            category.addProperty("lootChestCloseOverride", attempts == 0 ? FavoriteCategory.NEVER_CLOSE : attempts);
        }

        JsonArray categories = new JsonArray();
        categories.add(category);

        JsonObject lootFilter = new JsonObject();
        lootFilter.addProperty("name", LootFilter.DEFAULT_NAME);
        lootFilter.add("categories", categories);

        JsonArray lootFilters = new JsonArray();
        lootFilters.add(lootFilter);
        configObject.add(LOOT_FILTERS_KEY, lootFilters);

        return true;
    }
}
