/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

import com.wynntils.core.components.Manager;
import com.wynntils.wynn.gear.stats.DamageStatBuilder;
import com.wynntils.wynn.gear.stats.DefenceStatBuilder;
import com.wynntils.wynn.gear.stats.MiscStatBuilder;
import com.wynntils.wynn.gear.stats.SpellStatBuilder;
import com.wynntils.wynn.gear.stats.StatBuilder;
import com.wynntils.wynn.gear.types.GearStat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GearInfoManager extends Manager {
    private static final List<StatBuilder> STAT_BUILDERS =
            List.of(new MiscStatBuilder(), new DefenceStatBuilder(), new SpellStatBuilder(), new DamageStatBuilder());

    public final List<GearStat> registry = new ArrayList<>();
    public final Map<String, GearStat> lookup = new HashMap<>();

    public GearInfoManager() {
        super(List.of());

        for (StatBuilder builder : STAT_BUILDERS) {
            builder.buildStats(registry::add);
        }

        // Create a fast lookup map
        for (GearStat stat : registry) {
            String lookupName = stat.displayName() + stat.unit().getDisplayName();
            lookup.put(lookupName, stat);
        }
    }

    public GearStat getGearStat(String displayName, String unit) {
        String lookupName = displayName + unit;
        return lookup.get(lookupName);
    }
}
