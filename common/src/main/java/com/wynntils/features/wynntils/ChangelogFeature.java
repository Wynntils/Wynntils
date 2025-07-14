/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.screens.changelog.ChangelogScreen;
import com.wynntils.utils.mc.McUtils;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class ChangelogFeature extends Feature {
    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldState.CHARACTER_SELECTION) return;
        if (WynntilsMod.getVersion().equals(Services.Update.lastShownChangelogVersion.get())) return;

        Services.Update.getChangelog(true).thenAccept(changelog -> {
            Managers.TickScheduler.scheduleNextTick(() -> McUtils.mc().setScreen(ChangelogScreen.create(changelog)));
        });
    }
}
