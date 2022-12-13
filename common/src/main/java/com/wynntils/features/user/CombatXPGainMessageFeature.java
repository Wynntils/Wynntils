/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.event.SetExperienceEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CombatXPGainMessageFeature extends UserFeature {
    @Config
    public float secondDelay = 0.5f;

    private long lastExperienceDisplayTime = 0;

    private float newTickXP = 0;
    private float lastTickXP = 0;
    private float trackedPercentage = 0;
    private int trackedLevel = 0;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        lastExperienceDisplayTime = 0;
        newTickXP = 0;
        lastTickXP = 0;
        trackedPercentage = 0;
    }

    @SubscribeEvent
    public void onExperienceChange(SetExperienceEvent event) {
        if (!WynnUtils.onWorld()) return;

        if (System.currentTimeMillis() - lastExperienceDisplayTime < secondDelay * 1000) return;

        gatherAndDispatchMessage();
    }

    private void gatherAndDispatchMessage() {
        CharacterManager.CharacterInfo data = WynnUtils.getCharacterInfo();

        int newLevel = data.getXpLevel();

        if (trackedLevel == 0) {
            trackedLevel = newLevel;
        }

        // Handle levelling up in an active session, otherwise you might see a message like "+500 XP (-90.37%)"
        if (newLevel != trackedLevel) {
            trackedLevel = newLevel;
            lastTickXP = 0;
            trackedPercentage = 0;
        }

        newTickXP = data.getCurrentXp();

        if (newTickXP == lastTickXP) return;

        int neededXP = data.getXpPointsNeededToLevelUp();

        // Something went wrong, or you're at the level cap.
        if (neededXP == 0) return;

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

        // If the gain, rounded to 2 decimals is 0, we should not display it.
        if (Math.round(gainedXP * 100) / 100 == 0) return;

        // Only set this here, so we do not display the experience gains every x seconds,
        // but display it instantly when we first get one, then every x seconds after that.
        lastExperienceDisplayTime = System.currentTimeMillis();

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
