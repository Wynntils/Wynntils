/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import net.minecraft.world.item.ItemStack;

public interface BeaconKind {
    boolean matches(ItemStack itemStack);

    float getCustomModelData();
}
