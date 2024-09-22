/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ContainerLimitStackSizeEvent;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class FixStackSizeFeature extends Feature {
    @SubscribeEvent
    public void onStackSizeLimit(ContainerLimitStackSizeEvent event) {
        event.setCanceled(true);
    }
}
