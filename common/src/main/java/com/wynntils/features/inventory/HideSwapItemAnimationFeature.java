/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ItemSwapHandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class HideSwapItemAnimationFeature extends Feature {
    public HideSwapItemAnimationFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onItemSwap(ItemSwapHandsEvent event) {
        event.setCanceled(true);
    }
}
