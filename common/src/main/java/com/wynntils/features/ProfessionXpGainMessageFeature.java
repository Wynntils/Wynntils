/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.notifications.MessageContainer;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.profession.event.ProfessionXpGainEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class ProfessionXpGainMessageFeature extends Feature {
    @Persisted
    public final Config<Float> secondDelay = new Config<>(15f);

    @Persisted
    public final Config<Boolean> filterChat = new Config<>(true);

    private long lastXpDisplayTime = 0;
    private MessageContainer lastMessage = null;
    private float lastRawXpGain = 0;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        lastXpDisplayTime = 0;
    }

    @SubscribeEvent
    public void onExperienceGain(ProfessionXpGainEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (lastMessage != null && System.currentTimeMillis() - lastXpDisplayTime < secondDelay.get() * 1000) {
            lastRawXpGain += event.getGainedXpRaw();
            Managers.Notification.editMessage(lastMessage, getXpGainMessage(event));
        } else {
            lastRawXpGain = event.getGainedXpRaw();
            lastMessage = Managers.Notification.queueMessage(getXpGainMessage(event));
        }
        lastXpDisplayTime = System.currentTimeMillis();

        if (filterChat.get()) {
            event.setCanceled(true);
        }
    }

    private StyledText getXpGainMessage(ProfessionXpGainEvent event) {
        return StyledText.fromString(String.format(
                "§2+%.0f %s XP (§6%.2f%%§2)",
                lastRawXpGain, event.getProfession().getProfessionIconChar(), event.getCurrentXpPercentage()));
    }
}
