/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.models.characterstats.actionbar.matchers.CombatExperienceSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.CombatExperienceSegment;
import com.wynntils.models.characterstats.event.CombatXpGainEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.TimedSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.neoforged.bus.api.SubscribeEvent;

public final class CombatXpModel extends Model {
    /*
     * These values were provided by HeyZeer0 in #artemis-dev
     * Note that the last value is the sum of all preceding values
     */
    private static final int[] LEVEL_UP_XP_REQUIREMENTS = {
        150, 320, 520, 730, 950, 1060, 1260, 1300, 1500, 1850, 2110, 2400, 2760, 3010, 3380, 3810, 4200, 4790, 5490,
        5730, 6310, 6820, 7750, 8450, 9700, 10500, 12000, 13700, 15400, 16600, 18800, 21200, 24000, 25200, 28400, 30000,
        33500, 35000, 39000, 43000, 48000, 57300, 63500, 75000, 83000, 92000, 101000, 119000, 131000, 145000, 160000,
        185000, 205000, 225000, 246000, 285000, 313000, 340000, 375000, 432200, 472300, 515800, 562800, 613700, 668600,
        728000, 792000, 860000, 935000, 1040400, 1154400, 1282600, 1414800, 1567500, 1730400, 1837000, 1954800, 2077600,
        2194400, 2325600, 2455000, 2645000, 2845000, 3141100, 3404710, 3782160, 4151400, 4604100, 5057300, 5533840,
        6087120, 6685120, 7352800, 8080800, 8725600, 9578400, 10545600, 11585600, 12740000, 14418250, 16280000,
        21196500, 23315500, 25649000, 248721250
    };
    private static final int MAX_LEVEL = 106;

    private float lastTickXp = 0;
    private int trackedLevel = 0;
    private float currentLevelProgress = 0;

    private boolean firstJoinHappened = false;

    private final TimedSet<Float> rawXpGainInLastMinute = new TimedSet<>(1, TimeUnit.MINUTES, true);
    private final TimedSet<Float> percentageXpGainInLastMinute = new TimedSet<>(1, TimeUnit.MINUTES, true);

    public CombatXpModel() {
        super(List.of());

        // Register relevant action bar segments
        Handlers.ActionBar.registerSegment(new CombatExperienceSegmentMatcher());
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresent(CombatExperienceSegment.class, this::updateCombatExperience);
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

    public CappedValue getCombatLevel() {
        return new CappedValue(Models.CharacterStats.getLevel(), MAX_LEVEL);
    }

    public CappedValue getXp() {
        return CappedValue.fromProgress(currentLevelProgress, getTotalXpPointsNeededToLevelUp());
    }

    public int getXpPointsNeededToLevelUp() {
        return getTotalXpPointsNeededToLevelUp() - getXp().current();
    }

    private int getTotalXpPointsNeededToLevelUp() {
        int levelIndex = Models.CharacterStats.getLevel() - 1;
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

    private void updateCombatExperience(CombatExperienceSegment combatExperienceSegment) {
        // We calculate progress from the segment, as we can get a somewhat precise value from it.
        float progress = (float) combatExperienceSegment.getProgress().getProgress();

        // Note: There used to be progress correction here.
        //       It was removed with 2.1.2_5 as the calculation was not correct anymore.
        //       The progress is a off by a percentage usually, but currently there is no way to correct it.

        currentLevelProgress = progress;

        // On first world join, we get all our current XP points (the currently gained amount for the next level), but
        // we only care about actual gains
        if (!firstJoinHappened) {
            lastTickXp = getXp().current();
            firstJoinHappened = true;
            return;
        }

        float newTickXp = getXp().current();

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

        int neededXp = getTotalXpPointsNeededToLevelUp();

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
}
