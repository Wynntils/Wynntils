/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.screens.changelog.ChangelogScreen;
import com.wynntils.utils.mc.McUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChangelogFeature extends UserFeature {
    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.isFirstJoinWorld()) {
            McUtils.mc().setScreen(ChangelogScreen.create());
        }
    }
}
