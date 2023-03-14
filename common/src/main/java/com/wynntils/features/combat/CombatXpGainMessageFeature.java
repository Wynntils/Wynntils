/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.models.characterstats.event.CombatXpGainEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class CombatXpGainMessageFeature extends Feature {
    @RegisterConfig
    public final Config<Float> secondDelay = new Config<>(0.5f);

    private long lastXpDisplayTime = 0;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        lastXpDisplayTime = 0;
    }

    @SubscribeEvent
    public void onExperienceGain(CombatXpGainEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (System.currentTimeMillis() - lastXpDisplayTime < secondDelay.get() * 1000) return;

        String message =
                String.format("§2+%.0f XP (§6%.2f%%§2)", event.getGainedXpRaw(), event.getGainedXpPercentage());

        Managers.Notification.queueMessage(message);
    }
}
