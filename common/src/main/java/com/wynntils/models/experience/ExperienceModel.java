/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.experience;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.SetXpEvent;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.experience.event.ExperienceGainEvent;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import java.util.List;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ExperienceModel extends Model {
    private float lastTickXp = 0;
    private int trackedLevel = 0;

    private boolean firstJoinHappened = false;

    public ExperienceModel(CharacterModel characterModel, WorldStateModel worldStateModel) {
        super(List.of(characterModel, worldStateModel));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldState.WORLD) {
            firstJoinHappened = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onXpChange(SetXpEvent event) {
        if (!Models.WorldState.onWorld()) return;

        // We don't want to track XP before we've even got a packet to set our level
        if (Models.Character.getXpLevel() == 0) return;

        // On first world join, we get all our current XP points (the currently gained amount for the next level), but
        // we only care about actual gains
        if (!firstJoinHappened) {
            lastTickXp = Models.Character.getCurrentXp();
            firstJoinHappened = true;
            return;
        }

        float newTickXp = Models.Character.getCurrentXp();

        if (newTickXp == lastTickXp) return;

        int newLevel = Models.Character.getXpLevel();

        if (trackedLevel == 0) {
            trackedLevel = newLevel;
        }

        // Handle levelling up in an active session, otherwise you might see a message like "+500 XP (-90.37%)"
        if (newLevel != trackedLevel) {
            trackedLevel = newLevel;
            lastTickXp = 0;
        }

        int neededXp = Models.Character.getXpPointsNeededToLevelUp();

        // Something went wrong, or you're at the level cap.
        if (neededXp == 0) return;

        // The purpose of this if/else statement here is to account for the case when a player joins a
        // Wynncraft world and the lastTickXP variable is still 0, because it hasn't been updated yet.
        // Wynncraft will send the saved XP points to the player within a few ticks of joining the world,
        // but lastTickXP will still equal 0. So, what we do here to ensure that we don't get some message
        // like "+56403 XP (0.0%)" is to just gather the percentage from the newTickXP variable instead.
        // Thus giving us a message like "+56403 XP (42.7%)".
        float trackedPercentage;
        if (lastTickXp != 0) {
            trackedPercentage = lastTickXp / neededXp;
        } else {
            trackedPercentage = newTickXp / neededXp;
        }

        float gainedXP = newTickXp - lastTickXp;

        // If the gain, rounded to 2 decimals is 0, we should not display it.
        if (Math.round(gainedXP * 100) / 100 == 0) return;

        float percentGained = gainedXP / neededXp;

        float percentChange;
        // The reason we do this is for the same reason as the if/else statement above. You can get cases where
        // both trackedPercentage and percentGained are equal, which would result in a (0.0%) message.
        if (trackedPercentage != percentGained) {
            percentChange = percentGained * 100;
        } else {
            percentChange = trackedPercentage * 100;
        }

        lastTickXp = newTickXp;

        WynntilsMod.postEvent(new ExperienceGainEvent(gainedXP, percentChange));
    }
}
