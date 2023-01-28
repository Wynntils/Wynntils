/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import com.wynntils.core.components.Model;
import com.wynntils.models.gearinfo.GearInfo;
import com.wynntils.models.stats.builders.DamageStatBuilder;
import com.wynntils.models.stats.builders.DefenceStatBuilder;
import com.wynntils.models.stats.builders.MiscStatBuilder;
import com.wynntils.models.stats.builders.MiscStatKind;
import com.wynntils.models.stats.builders.SpellStatBuilder;
import com.wynntils.models.stats.type.DamageStatType;
import com.wynntils.models.stats.type.DefenceStatType;
import com.wynntils.models.stats.type.MiscStatType;
import com.wynntils.models.stats.type.SpellStatType;
import com.wynntils.models.stats.type.StatListDelimiter;
import com.wynntils.models.stats.type.StatType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatModel extends Model {
    public static final List<MiscStatKind> WYNNCRAFT_MISC_ORDER_1 = List.of(
            MiscStatKind.HEALTH_REGEN_PERCENT,
            MiscStatKind.MANA_REGEN,
            MiscStatKind.LIFE_STEAL,
            MiscStatKind.MANA_STEAL,
            MiscStatKind.XP_BONUS,
            MiscStatKind.LOOT_BONUS,
            MiscStatKind.REFLECTION,
            MiscStatKind.THORNS,
            MiscStatKind.EXPLODING,
            MiscStatKind.WALK_SPEED,
            MiscStatKind.ATTACK_SPEED,
            MiscStatKind.POISON,
            MiscStatKind.HEALTH,
            MiscStatKind.SOUL_POINT_REGEN,
            MiscStatKind.STEALING,
            MiscStatKind.HEALTH_REGEN_RAW);
    public static final List<MiscStatKind> WYNNCRAFT_MISC_ORDER_2 =
            List.of(MiscStatKind.SPRINT, MiscStatKind.SPRINT_REGEN);
    public static final List<MiscStatKind> WYNNCRAFT_MISC_ORDER_3 = List.of(
            MiscStatKind.JUMP_HEIGHT,
            MiscStatKind.GATHER_XP_BONUS,
            MiscStatKind.GATHER_SPEED,
            MiscStatKind.LOOT_QUALITY);

    private final List<StatType> statTypeRegistry = new ArrayList<>();
    private final Map<String, StatType> statLookup = new HashMap<>();

    public final List<StatType> defaultOrder = new ArrayList<>();
    public final List<StatType> wynntilsOrder = new ArrayList<>();
    public final List<StatType> legacyOrder = new ArrayList<>();

    public StatModel() {
        List<DefenceStatType> defenceStats = DefenceStatBuilder.createStats();
        List<MiscStatType> miscStats = MiscStatBuilder.createStats();
        List<DamageStatType> damageStats = DamageStatBuilder.createStats();
        List<SpellStatType> spellStats = SpellStatBuilder.createStats();

        // First build the complete registry
        statTypeRegistry.addAll(miscStats);
        statTypeRegistry.addAll(defenceStats);
        statTypeRegistry.addAll(damageStats);
        statTypeRegistry.addAll(spellStats);

        // Create a fast lookup map
        for (StatType stat : statTypeRegistry) {
            String lookupName = stat.getDisplayName() + stat.getUnit().getDisplayName();
            statLookup.put(lookupName, stat);
        }

        // Then create ordered lists for sorting

        // Default ordering is a lightly curated version of the Wynncraft vanilla ordering
        defaultOrder.addAll(miscStats);
        defaultOrder.add(new StatListDelimiter());
        defaultOrder.addAll(defenceStats);
        defaultOrder.add(new StatListDelimiter());
        defaultOrder.addAll(damageStats);
        defaultOrder.add(new StatListDelimiter());
        defaultOrder.addAll(spellStats);

        // Wynncraft order seem to have grown a bit haphazardly
        addMiscStats(wynntilsOrder, miscStats, WYNNCRAFT_MISC_ORDER_1);
        wynntilsOrder.add(new StatListDelimiter());
        wynntilsOrder.addAll(damageStats);
        wynntilsOrder.add(new StatListDelimiter());
        wynntilsOrder.addAll(defenceStats);
        wynntilsOrder.add(new StatListDelimiter());
        addMiscStats(wynntilsOrder, miscStats, WYNNCRAFT_MISC_ORDER_2);
        wynntilsOrder.add(new StatListDelimiter());
        wynntilsOrder.addAll(spellStats);
        wynntilsOrder.add(new StatListDelimiter());
        addMiscStats(wynntilsOrder, miscStats, WYNNCRAFT_MISC_ORDER_3);

        for (String apiName : LegacyStatListOrder.LEGACY_ORDER) {
            if (apiName.isEmpty()) {
                legacyOrder.add(new StatListDelimiter());
            } else {
                legacyOrder.addAll(fromApiName(apiName));
            }
        }
    }

    public List<StatType> getSortedStats(GearInfo gearInfo, List<StatType> ordering) {
        List<StatType> sortedStats = new ArrayList<>(gearInfo.getVariableStats());
        sortedStats.sort(Comparator.comparingInt(s -> ordering.indexOf(s)));
        return sortedStats;
    }

    private void addMiscStats(List<StatType> targetList, List<MiscStatType> miscStats, List<MiscStatKind> miscOrder) {
        for (MiscStatKind kind : miscOrder) {
            StatType stat = getMiscStat(kind, miscStats);
            targetList.add(stat);
        }
    }

    private StatType getMiscStat(MiscStatKind kind, List<MiscStatType> miscStats) {
        for (MiscStatType stat : miscStats) {
            if (stat.getKind() == kind) {
                return stat;
            }
        }
        return null;
    }

    // FIXME: No ideal design, used by deserialization
    public List<StatType> getStatTypeRegistry() {
        return statTypeRegistry;
    }

    public boolean isSpellStat(StatType stat) {
        // FIXME: Not very elegant...
        return stat.getApiName().startsWith("spellCost");
    }

    public StatType fromDisplayName(String displayName, String unit) {
        String lookupName = displayName + (unit == null ? "" : unit);
        return statLookup.get(lookupName);
    }

    public StatType fromLoreId(String id) {
        // FIXME: If this is a SpellStatType, we need to check the GearInfo. If it is a weapon,
        // return the proper type, otherwise return generic "3rd Spell". We cannot just return
        // the first value found.
        for (StatType stat : statTypeRegistry) {
            if (stat.getLoreName().equals(id)) return stat;
        }
        return null;
    }

    public List<StatType> fromApiName(String apiName) {
        List<StatType> stats = new ArrayList<>();
        // We might have many stats matching the same name (for spell cost stats)
        for (StatType stat : statTypeRegistry) {
            if (stat.getApiName().equals(apiName)) {
                stats.add(stat);
            }
        }
        return stats;
    }
}
