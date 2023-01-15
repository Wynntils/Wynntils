/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.types;

import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.objects.profiles.item.GearType;

public class GearMaterial {
    // FIXME: Somehow get this together so we can present a suitable item icon...

    public GearMaterial(String armorType, GearType gearType, CustomColor color) {
        // armorType is any of: CHAIN DIAMOND GOLDEN IRON LEATHER
    }

    public GearMaterial(GearType gearType) {
        // Material is missing, so just give generic icon for this type of gear (weapon or accessory)
    }

    public GearMaterial(String itemId, int damageCode) {
        // itemId is e.g. "stick", not "minecraft:stick"
    }
}
