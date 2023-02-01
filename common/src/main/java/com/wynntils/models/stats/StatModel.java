/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import com.wynntils.core.components.Model;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.models.stats.builders.DamageStatBuilder;
import com.wynntils.models.stats.builders.DefenceStatBuilder;
import com.wynntils.models.stats.builders.MiscStatBuilder;
import com.wynntils.models.stats.builders.SpellStatBuilder;
import com.wynntils.models.stats.builders.StatBuilder;
import com.wynntils.models.stats.type.DamageStatType;
import com.wynntils.models.stats.type.DefenceStatType;
import com.wynntils.models.stats.type.MiscStatType;
import com.wynntils.models.stats.type.SpellStatType;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatModel extends Model {
    private final List<StatType> statTypeRegistry = new ArrayList<>();
    private final StatLookupTable statTypeLookup = new StatLookupTable();
    private final Map<StatListOrdering, List<StatType>> orderingLists;

    public StatModel() {
        super(List.of());

        // First build stats of all kinds
        List<MiscStatType> miscStats = buildStats(new MiscStatBuilder());
        List<DefenceStatType> defenceStats = buildStats(new DefenceStatBuilder());
        List<DamageStatType> damageStats = buildStats(new DamageStatBuilder());
        List<SpellStatType> spellStats = buildStats(new SpellStatBuilder());

        // Then put them all in the registry
        initRegistry(miscStats, defenceStats, damageStats, spellStats);

        // Finally create ordered lists for sorting
        orderingLists = StatListOrderer.createOrderingMap(miscStats, defenceStats, damageStats, spellStats);
    }

    public StatType fromDisplayName(String displayName, String unit) {
        return statTypeLookup.get(displayName, unit);
    }

    public StatType fromInternalRollId(String id) {
        // FIXME: If this is a SpellStatType, we need to check the GearInfo. If it is a weapon,
        // return the proper type, otherwise return generic "3rd Spell". We cannot just return
        // the first value found.
        for (StatType stat : statTypeRegistry) {
            if (stat.getInternalRollName().equals(id)) return stat;
        }
        return null;
    }

    public String getDisplayName(StatType statType, GearInfo gearInfo) {
        ClassType classReq = gearInfo.type().getClassReq();
        if (classReq != null && statType instanceof SpellStatType spellStatType) {
            SpellType spellType = spellStatType.getSpellType().forOtherClass(classReq);
            return SpellStatBuilder.getStatNameFromSpell(spellType.getName());
        }
        return statType.getDisplayName();
    }

    public List<StatType> getOrderingList(StatListOrdering ordering) {
        return orderingLists.get(ordering);
    }

    public List<StatType> getSortedStats(GearInfo gearInfo, StatListOrdering ordering) {
        List<StatType> orderingList = orderingLists.get(ordering);

        List<StatType> sortedStats = new ArrayList<>(gearInfo.getVariableStats());
        sortedStats.sort(Comparator.comparingInt(orderingList::indexOf));

        return sortedStats;
    }

    public List<StatType> getAllStatTypes() {
        return statTypeRegistry;
    }

    private static <T extends StatType> List<T> buildStats(StatBuilder<T> builder) {
        List<T> statList = new ArrayList<>();

        builder.buildStats(statList::add);
        return statList;
    }

    private void initRegistry(
            List<MiscStatType> miscStats,
            List<DefenceStatType> defenceStats,
            List<DamageStatType> damageStats,
            List<SpellStatType> spellStats) {

        statTypeRegistry.addAll(miscStats);
        statTypeRegistry.addAll(defenceStats);
        statTypeRegistry.addAll(damageStats);
        statTypeRegistry.addAll(spellStats);

        // Create a fast lookup map
        for (StatType stat : statTypeRegistry) {
            StatUnit unit = stat.getUnit();
            statTypeLookup.put(stat.getDisplayName(), unit, stat);
        }
        // Spell Cost stats have a lot of aliases under which they can appear
        for (SpellStatType stat : spellStats) {
            for (String alias : SpellStatBuilder.getAliases(stat)) {
                String lookupName = alias + stat.getUnit().getDisplayName();
                statTypeLookup.put(alias, stat.getUnit(), stat);
            }
        }
    }

    private static class StatLookupTable {
        private final Map<String, StatType> lookupTable = new HashMap<>();

        private StatType get(String displayName, String unit) {
            String lookupName = displayName + (unit == null ? "" : unit);
            return lookupTable.get(lookupName);
        }

        private void put(String displayName, StatUnit unit, StatType statType) {
            String lookupName = displayName + unit.getDisplayName();
            lookupTable.put(lookupName, statType);
        }
    }
}
