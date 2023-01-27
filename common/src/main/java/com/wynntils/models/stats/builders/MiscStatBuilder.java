/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.stats.type.MiscStatType;
import com.wynntils.models.stats.type.StatType;
import java.util.function.Consumer;

public final class MiscStatBuilder extends StatBuilder {
    @Override
    public void buildStats(Consumer<StatType> callback) {
        for (MiscStatKind statType : MiscStatKind.values()) {
            StatType gearStat = new MiscStatType(
                    "MISC_" + statType.name(),
                    statType.getDisplayName(),
                    statType.getApiName(),
                    statType.getLoreName(),
                    statType.getUnit());
            callback.accept(gearStat);
        }
    }
}
