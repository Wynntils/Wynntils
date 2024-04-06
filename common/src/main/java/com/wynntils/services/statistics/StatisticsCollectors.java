/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.statistics;

import com.wynntils.core.components.Services;
import com.wynntils.models.damage.type.DamageDealtEvent;
import com.wynntils.models.lootrun.event.LootrunFinishedEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.services.statistics.type.StatisticKind;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class StatisticsCollectors {
    @SubscribeEvent
    public void onDamageDealtEvent(DamageDealtEvent event) {
        int neutralDamage = event.getDamages().getOrDefault(DamageType.ALL, 0);
        Services.Statistics.addToStatistics(StatisticKind.DAMAGE_DEALT, neutralDamage);
    }

    @SubscribeEvent
    public void onSpellEvent(SpellEvent.Completed event) {
        Services.Statistics.increaseStatistics(StatisticKind.SPELLS_CAST);
    }

    @SubscribeEvent
    public void onLootrunCompleted(LootrunFinishedEvent.Completed event) {
        Services.Statistics.increaseStatistics(StatisticKind.LOOTRUNS_COMPLETED);

        Services.Statistics.addToStatistics(
                StatisticKind.LOOTRUNS_CHALLENGES_COMPLETED, event.getChallengesCompleted());
        Services.Statistics.addToStatistics(StatisticKind.LOOTRUNS_TIME_ELAPSED, event.getTimeElapsed());
        Services.Statistics.addToStatistics(StatisticKind.LOOTRUNS_REWARD_PULLS, event.getRewardPulls());
        Services.Statistics.addToStatistics(StatisticKind.LOOTRUNS_REWARD_REROLLS, event.getRewardRerolls());
        Services.Statistics.addToStatistics(StatisticKind.LOOTRUNS_EXPERIENCE_GAINED, event.getExperienceGained());
        Services.Statistics.addToStatistics(StatisticKind.LOOTRUNS_MOBS_KILLED, event.getMobsKilled());
    }

    @SubscribeEvent
    public void onLootrunFailed(LootrunFinishedEvent.Failed event) {
        Services.Statistics.increaseStatistics(StatisticKind.LOOTRUNS_FAILED);

        Services.Statistics.addToStatistics(
                StatisticKind.LOOTRUNS_CHALLENGES_COMPLETED, event.getChallengesCompleted());
        Services.Statistics.addToStatistics(StatisticKind.LOOTRUNS_TIME_ELAPSED, event.getTimeElapsed());
    }
}
