/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import java.util.List;

public class GearMiscStatBuilder {
    public static void addStats(List<GearStat> registry) {
        for (GearMiscStatType miscStat : GearMiscStatType.values()) {
            GearStat holder = new GearStat(
                    "MISC_" + miscStat.name(),
                    miscStat.getDisplayName(),
                    miscStat.getApiName(),
                    miscStat.getLoreName(),
                    miscStat.getUnit());
            registry.add(holder);
        }
    }
}
