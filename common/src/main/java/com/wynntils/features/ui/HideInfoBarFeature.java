/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.bossbar.event.BossBarAddedEvent;
import com.wynntils.models.worlds.bossbars.InfoBar;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.UI)
public class HideInfoBarFeature extends Feature {
    @SubscribeEvent
    public void onBossBarAdd(BossBarAddedEvent event) {
        if (event.getTrackedBar().getClass().equals(InfoBar.class)) {
            event.setCanceled(true);
        }
    }
}
