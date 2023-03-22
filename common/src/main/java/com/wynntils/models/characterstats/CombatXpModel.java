/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.SetXpEvent;
import com.wynntils.models.characterstats.event.CombatXpGainEvent;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.TimedSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CombatXpModel extends Model {
    /* These values are copied from a post by Salted, https://forums.wynncraft.com/threads/new-levels-xp-requirement.261763/
     * which points to the data at https://pastebin.com/fCTfEkaC
     * Note that the last value is the sum of all preceding values
     */
    private static final int[] LEVEL_UP_XP_REQUIREMENTS = {
        110, 190, 275, 385, 505, 645, 790, 940, 1100, 1370, 1570, 1800, 2090, 2400, 2720, 3100, 3600, 4150, 4800, 5300,
        5900, 6750, 7750, 8900, 10200, 11650, 13300, 15200, 17150, 19600, 22100, 24900, 28000, 31500, 35500, 39900,
        44700, 50000, 55800, 62000, 68800, 76400, 84700, 93800, 103800, 114800, 126800, 140000, 154500, 170300, 187600,
        206500, 227000, 249500, 274000, 300500, 329500, 361000, 395000, 432200, 472300, 515800, 562800, 613700, 668600,
        728000, 792000, 860000, 935000, 1040400, 1154400, 1282600, 1414800, 1567500, 1730400, 1837000, 1954800, 2077600,
        2194400, 2325600, 2455000, 2645000, 2845000, 3141100, 3404710, 3782160, 4151400, 4604100, 5057300, 5533840,
        6087120, 6685120, 7352800, 8080800, 8725600, 9578400, 10545600, 11585600, 12740000, 14418250, 16280000,
        21196500, 23315500, 25649000, 249232940
    };
    private static final int MAX_LEVEL = 106;

    private float lastTickXp = 0;
    private int trackedLevel = 0;

    private boolean firstJoinHappened = false;

    private final TimedSet<Float> rawXpGainInLastMinute = new TimedSet<>(1, TimeUnit.MINUTES, true);
    private final TimedSet<Float> percentageXpGainInLastMinute = new TimedSet<>(1, TimeUnit.MINUTES, true);

    public CombatXpModel(WorldStateModel worldStateModel) {
        super(List.of(worldStateModel));
    }

    @SubscribeEvent
    public void onXpGain(CombatXpGainEvent event) {
        rawXpGainInLastMinute.put(event.getGainedXpRaw());
        percentageXpGainInLastMinute.put(event.getGainedXpPercentage());
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
        if (McUtils.player().experienceLevel == 0) return;

        // On first world join, we get all our current XP points (the currently gained amount for the next level), but
        // we only care about actual gains
        if (!firstJoinHappened) {
            lastTickXp = getCurrentXpAsFloat();
            firstJoinHappened = true;
            return;
        }

        float newTickXp = getCurrentXpAsFloat();

        if (newTickXp == lastTickXp) return;

        int newLevel = getCombatLevel().current();

        if (trackedLevel == 0) {
            trackedLevel = newLevel;
        }

        // Handle levelling up in an active session, otherwise you might see a message like "+500 XP (-90.37%)"
        if (newLevel != trackedLevel) {
            trackedLevel = newLevel;
            lastTickXp = 0;
        }

        int neededXp = getXpPointsNeededToLevelUp();

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

        WynntilsMod.postEvent(new CombatXpGainEvent(gainedXP, percentChange));
    }

    public CappedValue getCombatLevel() {
        return new CappedValue(McUtils.player().experienceLevel, MAX_LEVEL);
    }

    public CappedValue getXp() {
        return new CappedValue((int) getCurrentXpAsFloat(), getXpPointsNeededToLevelUp());
    }

    private float getCurrentXpAsFloat() {
        // We calculate our level in points by seeing how far we've progress towards our
        // current XP level's max
        return McUtils.player().experienceProgress * getXpPointsNeededToLevelUp();
    }

    private int getXpPointsNeededToLevelUp() {
        int levelIndex = McUtils.player().experienceLevel - 1;
        if (levelIndex >= LEVEL_UP_XP_REQUIREMENTS.length) {
            return Integer.MAX_VALUE;
        }
        if (levelIndex < 0) {
            return 0;
        }
        return LEVEL_UP_XP_REQUIREMENTS[levelIndex];
    }

    public TimedSet<Float> getRawXpGainInLastMinute() {
        return rawXpGainInLastMinute;
    }

    public TimedSet<Float> getPercentageXpGainInLastMinute() {
        return percentageXpGainInLastMinute;
    }

    public long getLastXpGainTimestamp() {
        return rawXpGainInLastMinute.getLastAddedTimestamp();
    }
}
