/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.SetXpEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CombatXpGainMessageFeature extends UserFeature {
    @Config
    public float secondDelay = 0.5f;

    private long lastXpDisplayTime = 0;
    private float newTickXp = 0;
    private float lastTickXp = 0;
    private float trackedPercentage = 0;
    private int trackedLevel = 0;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        lastXpDisplayTime = 0;
        newTickXp = 0;
        lastTickXp = 0;
        trackedPercentage = 0;
    }

    @SubscribeEvent
    public void onXpChange(SetXpEvent event) {
        if (!Models.WorldState.onWorld()) return;

        if (System.currentTimeMillis() - lastXpDisplayTime < secondDelay * 1000) return;

        gatherAndDispatchMessage();
    }

    private void gatherAndDispatchMessage() {
        int newLevel = Models.Character.getXpLevel();

        if (trackedLevel == 0) {
            trackedLevel = newLevel;
        }

        // Handle levelling up in an active session, otherwise you might see a message like "+500 XP (-90.37%)"
        if (newLevel != trackedLevel) {
            trackedLevel = newLevel;
            lastTickXp = 0;
            trackedPercentage = 0;
        }

        newTickXp = Models.Character.getCurrentXp();

        if (newTickXp == lastTickXp) return;

        int neededXp = Models.Character.getXpPointsNeededToLevelUp();

        // Something went wrong, or you're at the level cap.
        if (neededXp == 0) return;

        // The purpose of this if/else statement here is to account for the case when a player joins a
        // Wynncraft world and the lastTickXP variable is still 0, because it hasn't been updated yet.
        // Wynncraft will send the saved XP points to the player within a few ticks of joining the world,
        // but lastTickXP will still equal 0. So, what we do here to ensure that we don't get some message
        // like "+56403 XP (0.0%)" is to just gather the percentage from the newTickXP variable instead.
        // Thus giving us a message like "+56403 XP (42.7%)".
        if (lastTickXp != 0) {
            trackedPercentage = lastTickXp / neededXp;
        } else {
            trackedPercentage = newTickXp / neededXp;
        }

        float gainedXP = newTickXp - lastTickXp;

        // If the gain, rounded to 2 decimals is 0, we should not display it.
        if (Math.round(gainedXP * 100) / 100 == 0) return;

        // Only set this here, so we do not display the experience gains every x seconds,
        // but display it instantly when we first get one, then every x seconds after that.
        lastXpDisplayTime = System.currentTimeMillis();

        float percentGained = gainedXP / neededXp;

        float percentChange;
        // The reason we do this is for the same reason as the if/else statement above. You can get cases where
        // both trackedPercentage and percentGained are equal, which would result in a (0.0%) message.
        if (trackedPercentage != percentGained) {
            percentChange = percentGained * 100;
        } else {
            percentChange = trackedPercentage * 100;
        }

        String message = String.format("§2+%.0f XP (§6%.2f%%§2)", gainedXP, percentChange);

        lastTickXp = newTickXp;

        Managers.Notification.queueMessage(message);
    }
}
