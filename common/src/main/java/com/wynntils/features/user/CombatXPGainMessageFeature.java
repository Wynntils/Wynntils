/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CombatXPGainMessageFeature extends UserFeature {

    @Config
    public int tickDelay = 20;

    private long tickCounter = 0;

    private float newTickXP = 0;
    private float lastTickXP = 0;
    private float trackedPercentage = 0;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        tickCounter = 0;
        newTickXP = 0;
        lastTickXP = 0;
        trackedPercentage = 0;
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.End event) {
        if (!WynnUtils.onWorld()) return;

        tickCounter++;

        if (tickCounter % tickDelay != 0) return;

        tickCounter = 0;

        CharacterManager.CharacterInfo data = WynnUtils.getCharacterInfo();

        int level = data.getXpLevel();

        // You get division by zero errors when you're at the level cap (i.e. 106 in Wynncraft 2.0.1).
        // This needs to be updated if the level cap is ever raised.
        if (level > 105) return;

        newTickXP = data.getCurrentXp();

        if (newTickXP == lastTickXP) return;

        int neededXP = data.getXpPointsNeededToLevelUp();
        // The purpose of this if/else statement here is to account for the case when a player joins a
        // Wynncraft world and the lastTickXP variable is still 0, because it hasn't been updated yet.
        // Wynncraft will send the saved XP points to the player within a few ticks of joining the world,
        // but lastTickXP will still equal 0. So, what we do here to ensure that we don't get some message
        // like "+56403 XP (0.0%)" is to just gather the percentage from the newTickXP variable instead.
        // Thus giving us a message like "+56403 XP (42.7%)".
        if (lastTickXP != 0) {
            trackedPercentage = lastTickXP / neededXP;
        } else {
            trackedPercentage = newTickXP / neededXP;
        }

        float gainedXP = newTickXP - lastTickXP;

        float percentGained = gainedXP / neededXP;

        float percentChange;
        // The reason we do this is for the same reason as the if/else statement above. You can get cases where
        // both trackedPercentage and percentGained are equal, which would result in a (0.0%) message.
        if (trackedPercentage != percentGained) {
            percentChange = percentGained * 100;
        } else {
            percentChange = trackedPercentage * 100;
        }

        String message = String.format("§2+%.0f XP (§6%.2f%%§2)", gainedXP, percentChange);

        lastTickXP = newTickXP;

        NotificationManager.queueMessage(message);
    }
}
