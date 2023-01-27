/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import com.wynntils.core.components.Model;
import com.wynntils.models.stats.builders.DamageStatBuilder;
import com.wynntils.models.stats.builders.DefenceStatBuilder;
import com.wynntils.models.stats.builders.MiscStatBuilder;
import com.wynntils.models.stats.builders.SpellStatBuilder;
import com.wynntils.models.stats.builders.StatBuilder;
import com.wynntils.models.stats.type.StatType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatModel extends Model {
    private static final List<StatBuilder> STAT_BUILDERS =
            List.of(new MiscStatBuilder(), new DefenceStatBuilder(), new SpellStatBuilder(), new DamageStatBuilder());

    private final List<StatType> statTypeRegistry = new ArrayList<>();
    private final Map<String, StatType> statLookup = new HashMap<>();

    public StatModel() {
        for (StatBuilder builder : STAT_BUILDERS) {
            builder.buildStats(statTypeRegistry::add);
        }

        // Create a fast lookup map
        for (StatType stat : statTypeRegistry) {
            String lookupName = stat.getDisplayName() + stat.getUnit().getDisplayName();
            statLookup.put(lookupName, stat);
        }
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
