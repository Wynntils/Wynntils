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
import com.wynntils.models.stats.builders.SpellStatBuilder;
import com.wynntils.models.stats.type.DamageStatType;
import com.wynntils.models.stats.type.DefenceStatType;
import com.wynntils.models.stats.type.MiscStatType;
import com.wynntils.models.stats.type.SpellStatType;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatModel extends Model {
    private final List<StatType> statTypeRegistry = new ArrayList<>();
    private final Map<String, StatType> statTypeLookup = new HashMap<>();

    private final Map<StatListOrdering, List<StatType>> orderingLists;

    public StatModel() {
        // First build stats of all kinds
        List<MiscStatType> miscStats = MiscStatBuilder.createStats();
        List<DefenceStatType> defenceStats = DefenceStatBuilder.createStats();
        List<DamageStatType> damageStats = DamageStatBuilder.createStats();
        List<SpellStatType> spellStats = SpellStatBuilder.createStats();

        // Then put them all in the registry
        initRegistry(miscStats, defenceStats, damageStats, spellStats);

        // Finally create ordered lists for sorting
        orderingLists = StatListOrderer.createOrderingMap(miscStats, defenceStats, damageStats, spellStats);
    }

    public StatType fromDisplayName(String displayName, String unit) {
        String lookupName = displayName + (unit == null ? "" : unit);
        return statTypeLookup.get(lookupName);
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
            String lookupName = stat.getDisplayName() + stat.getUnit().getDisplayName();
            statTypeLookup.put(lookupName, stat);
        }
    }
}
