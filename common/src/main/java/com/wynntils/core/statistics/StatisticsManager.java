/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.statistics;

import com.wynntils.core.components.Manager;
import com.wynntils.models.damage.type.DamageDealtEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.stats.type.DamageType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// This should really be an "ExternalModel"...
public final class StatisticsManager extends Manager {
    private final Map<StatisticKind, Integer> statistics = new EnumMap<>(StatisticKind.class);

    public StatisticsManager() {
        super(List.of());
    }

    public void increaseStatistics(StatisticKind kind) {
        addToStatistics(kind, 1);
    }

    public void addToStatistics(StatisticKind kind, int amount) {
        statistics.put(kind, statistics.getOrDefault(kind, 0) + amount);
    }

    public int getStatistic(StatisticKind statistic) {
        return statistics.getOrDefault(statistic, 0);
    }

    public void resetStatistics() {
        statistics.clear();
    }

    @SubscribeEvent
    public void onDamageDealtEvent(DamageDealtEvent event) {
        int neutralDamage = event.getDamages().getOrDefault(DamageType.ALL, 0);
        addToStatistics(StatisticKind.DAMAGE_DEALT, neutralDamage);
    }

    @SubscribeEvent
    public void onSpellEvent(SpellEvent.Completed event) {
        increaseStatistics(StatisticKind.SPELLS_CAST);
    }
}
