/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.stats.type.MiscStatType;
import java.util.function.Consumer;

public final class MiscStatBuilder extends StatBuilder<MiscStatType> {
    @Override
    public void buildStats(Consumer<MiscStatType> callback) {
        for (MiscStatKind kind : MiscStatKind.values()) {
            MiscStatType gearStat = new MiscStatType(
                    "MISC_" + kind.name(),
                    kind.getDisplayName(),
                    kind.getApiName(),
                    kind.getInternalRollName(),
                    kind.getUnit(),
                    kind);
            callback.accept(gearStat);
        }
    }
}
