/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import java.util.ArrayList;
import java.util.List;

public class GearStatRegistry {
    public static final List<GearStat> registry = new ArrayList<>();

    static {
        GearDamageStatBuilder.addStats(registry);
        GearDefenceStatBuilder.addStats(registry);
        GearSpellStatBuilder.addStats(registry);
        GearMiscStatBuilder.addStats(registry);
    }
}
