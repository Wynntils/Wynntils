/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import java.util.Set;

public class RemoveSetGearTierUpfixer implements Upfixer {
    private static final String ROOT_DRY_ITEM_TIERS_KEY = "model.lootChest.dryItemTiers";
    private static final String MYTHIC_FINDS_KEY = "model.lootChest.mythicFinds";
    private static final String DRY_ITEM_TIERS_KEY = "dryItemTiers";

    private static final String SET_KEY = "set";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        if (configObject.has(ROOT_DRY_ITEM_TIERS_KEY)) {
            JsonObject dryItemTiers = configObject.getAsJsonObject(ROOT_DRY_ITEM_TIERS_KEY);

            dryItemTiers.remove(SET_KEY);
        }

        if (configObject.has(MYTHIC_FINDS_KEY)) {
            JsonArray mythicFinds = configObject.getAsJsonArray(MYTHIC_FINDS_KEY);

            for (JsonElement element : mythicFinds) {
                JsonObject jsonObject = element.getAsJsonObject();

                if (jsonObject.has(DRY_ITEM_TIERS_KEY)) {
                    JsonObject dryItemTiers = jsonObject.getAsJsonObject(DRY_ITEM_TIERS_KEY);
                    dryItemTiers.remove(SET_KEY);
                }
            }
        }

        return true;
    }
}
