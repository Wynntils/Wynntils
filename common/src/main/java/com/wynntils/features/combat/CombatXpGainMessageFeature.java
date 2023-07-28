/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.notifications.MessageContainer;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.characterstats.event.CombatXpGainEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class CombatXpGainMessageFeature extends Feature {
    @RegisterConfig
    public final Config<Float> secondDelay = new Config<>(5f);

    private long lastXpDisplayTime = 0;

    private MessageContainer lastMessage = null;
    private float lastRawXpGain = 0;
    private float lastPercentageXpGain = 0;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        lastXpDisplayTime = 0;
    }

    @SubscribeEvent
    public void onExperienceGain(CombatXpGainEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (lastMessage != null && System.currentTimeMillis() - lastXpDisplayTime < secondDelay.get() * 1000) {
            Managers.Notification.editMessage(
                    lastMessage,
                    getXpGainMessage(
                            lastRawXpGain + event.getGainedXpRaw(),
                            lastPercentageXpGain + event.getGainedXpPercentage()));

            lastRawXpGain += event.getGainedXpRaw();
            lastPercentageXpGain += event.getGainedXpPercentage();
            lastXpDisplayTime = System.currentTimeMillis();

            return;
        }

        lastRawXpGain = event.getGainedXpRaw();
        lastPercentageXpGain = event.getGainedXpPercentage();
        lastXpDisplayTime = System.currentTimeMillis();

        lastMessage = Managers.Notification.queueMessage(
                getXpGainMessage(event.getGainedXpRaw(), event.getGainedXpPercentage()));
    }

    private static StyledText getXpGainMessage(float rawGain, float percentageGain) {
        return StyledText.fromString(String.format("§2+%.0f XP (§6%.2f%%§2)", rawGain, percentageGain));
    }
}
