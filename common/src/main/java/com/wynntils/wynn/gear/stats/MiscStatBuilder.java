/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import java.util.function.Consumer;

public final class MiscStatBuilder extends StatBuilder {
    @Override
    public void buildStats(Consumer<GearStat> callback) {
        for (GearMiscStatType statType : GearMiscStatType.values()) {
            GearStat gearStat = new GearStat(
                    "MISC_" + statType.name(),
                    statType.getDisplayName(),
                    statType.getApiName(),
                    statType.getLoreName(),
                    statType.getUnit());
            callback.accept(gearStat);
        }
    }
}
