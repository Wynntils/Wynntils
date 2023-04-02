/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.DrawPotionGlintEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class HidePotionGlintFeature extends Feature {
    @SubscribeEvent
    public void onPotionGlint(DrawPotionGlintEvent e) {
        e.setCanceled(true);
    }
}
