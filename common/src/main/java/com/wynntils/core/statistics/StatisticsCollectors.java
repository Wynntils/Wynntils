/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.statistics;

import com.wynntils.core.components.Managers;
import com.wynntils.models.damage.type.DamageDealtEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.stats.type.DamageType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class StatisticsCollectors {
    @SubscribeEvent
    public void onDamageDealtEvent(DamageDealtEvent event) {
        int neutralDamage = event.getDamages().getOrDefault(DamageType.ALL, 0);
        Managers.Statistics.addToStatistics(StatisticKind.DAMAGE_DEALT, neutralDamage);
    }

    @SubscribeEvent
    public void onSpellEvent(SpellEvent.Completed event) {
        Managers.Statistics.increaseStatistics(StatisticKind.SPELLS_CAST);
    }
}
