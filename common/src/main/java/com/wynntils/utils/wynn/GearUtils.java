/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.gearinfo.type.GearMaterial;
import java.util.Map;

public class GearUtils {
    private static final Map<Integer, String> KNOWN_USED_ITEM_CODES = Map.of(
            256,
            "iron_shovel", // Spear
            259,
            "flint_and_steel", // Accessories
            261,
            "bow", // Bow
            269,
            "wooden_shovel", // Wand
            273,
            "stone_shovel", // Relik
            359,
            "shears", // Dagger
            280,
            "stick", // Special items: Breaker Bar, Cracked Oak Wand, Sharpened Stylus, Valix
            352,
            "bone", // Special items: Wybel Paw
            86,
            "pumpkin" // Special items: Pumpkin Helmet
            );

    public static GearMaterial getItemFromCodeAndDamage(int itemTypeCode, int damageCode) {
        String itemId;
        if (itemTypeCode == 397 && damageCode == 2) {
            // Special case for Mama Zomble's memory
            itemId = "zombie_head";
        } else {
            // This is not ideal, but in practice just a subset of all mincraft items are used
            itemId = KNOWN_USED_ITEM_CODES.get(itemTypeCode);
            if (itemId == null) {
                WynntilsMod.warn(
                        "Could not convert item id: " + itemTypeCode + ":" + damageCode + " from gear database");
                itemId = "bedrock"; // whatever...
            }
        }
        return new GearMaterial(itemId, damageCode);
    }
}
