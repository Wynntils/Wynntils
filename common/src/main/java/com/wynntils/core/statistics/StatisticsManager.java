/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.statistics;

import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Models;
import com.wynntils.core.storage.Storage;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.damage.type.DamageDealtEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// This should really be an "ExternalModel"...
public final class StatisticsManager extends Manager {
    // All statistics, per character
    private final Storage<Map<String, Map<StatisticKind, Integer>>> statistics = new Storage<>(new TreeMap<>());

    // The currently active statistics
    private Map<StatisticKind, Integer> currentStatistics = new EnumMap<>(StatisticKind.class);

    public StatisticsManager() {
        super(List.of());
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent event) {
        if (!Models.Character.hasCharacter()) {
            // If we do not have a proper character, set up a fake statistics map so we always
            // have a valid map, otherwise we will crash when trying to set statistics.
            // These values will not be persisted.
            currentStatistics = new EnumMap<>(StatisticKind.class);
            return;
        }

        setCurrentStatistics(Models.Character.getId());
    }

    @SubscribeEvent
    public void onCharacterUpdated(CharacterUpdateEvent event) {
        setCurrentStatistics(Models.Character.getId());
    }

    public void increaseStatistics(StatisticKind kind) {
        addToStatistics(kind, 1);
    }

    public void addToStatistics(StatisticKind kind, int amount) {
        currentStatistics.put(kind, currentStatistics.getOrDefault(kind, 0) + amount);
        statistics.touched();
    }

    public int getStatistic(StatisticKind statistic) {
        return currentStatistics.getOrDefault(statistic, 0);
    }

    public void resetStatistics() {
        currentStatistics.clear();
        statistics.touched();
    }

    private void setCurrentStatistics(String id) {
        // Make sure our statistics is based on the correct character, and that it is persisted
        statistics.get().putIfAbsent(id, new EnumMap<>(StatisticKind.class));
        currentStatistics = statistics.get().get(id);
        statistics.touched();
    }

    // region Statistics collectors

    @SubscribeEvent
    public void onDamageDealtEvent(DamageDealtEvent event) {
        int neutralDamage = event.getDamages().getOrDefault(DamageType.ALL, 0);
        addToStatistics(StatisticKind.DAMAGE_DEALT, neutralDamage);
    }

    @SubscribeEvent
    public void onSpellEvent(SpellEvent.Completed event) {
        increaseStatistics(StatisticKind.SPELLS_CAST);
    }

    // endregion
}
