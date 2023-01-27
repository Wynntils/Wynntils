/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.gearinfo.type.GearMaterial;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.utils.type.RangedValue;
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

    public static RangedValue calculateRange(int baseValue, boolean preIdentified) {
        // FIXME: How does this work for stats where a negative value is good? (like spell cost)
        if (preIdentified) {
            // This is actually a single, fixed value
            return RangedValue.of(baseValue, baseValue);
        } else {
            if (baseValue > 0) {
                // Between 30% and 130% of base value, always at least 1
                int min = Math.max((int) Math.round(baseValue * 0.3), 1);
                int max = (int) Math.round(baseValue * 1.3);
                return RangedValue.of(min, max);
            } else {
                // Between 70% and 130% of base value, always at most -1
                // Round ties towards positive infinity (confirmed on Wynncraft)
                int min = (int) Math.round(baseValue * 1.3);
                int max = Math.min((int) Math.round(baseValue * 0.7), -1);
                return RangedValue.of(min, max);
            }
        }
    }

    // Calculate the range of possible values for the internal roll for this stat
    public RangedValue calculateInternalRoll(StatPossibleValues possibleValues, StatActualValue actualValue) {
        // FIXME
        return RangedValue.NONE;
    }

    public static GearMaterial getItemFromCodeAndDamage(int itemTypeCode, int damageCode) {
        String itemId;
        if (itemTypeCode == 392 && damageCode == 2) {
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

    public static int getStarsFromPercent(int intPercent) {
        // Star calculation reference, from salted:
        // https://forums.wynncraft.com/threads/about-the-little-asterisks.147931/#post-1654183
        int stars;
        if (intPercent < 101) {
            stars = 0;
        } else if (intPercent < 125) {
            stars = 1;
        } else if (intPercent < 130) {
            stars = 2;
        } else {
            stars = 3;
        }
        return stars;
    }
}
