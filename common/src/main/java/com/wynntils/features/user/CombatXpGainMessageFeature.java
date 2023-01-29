/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.models.experience.event.CombatXpGainEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CombatXpGainMessageFeature extends UserFeature {
    @Config
    public float secondDelay = 0.5f;

    private long lastXpDisplayTime = 0;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        lastXpDisplayTime = 0;
    }

    @SubscribeEvent
    public void onExperienceGain(CombatXpGainEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (System.currentTimeMillis() - lastXpDisplayTime < secondDelay * 1000) return;

        String message =
                String.format("§2+%.0f XP (§6%.2f%%§2)", event.getGainedXpRaw(), event.getGainedXpPercentage());

        Managers.Notification.queueMessage(message);
    }
}
