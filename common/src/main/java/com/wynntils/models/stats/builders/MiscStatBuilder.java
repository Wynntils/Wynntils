/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.stats.type.MiscStatType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class MiscStatBuilder extends StatBuilder<MiscStatType> {
    public static List<MiscStatType> createStats() {
        List<MiscStatType> statList = new ArrayList<>();

        MiscStatBuilder builder = new MiscStatBuilder();
        builder.buildStats(statList::add);
        return statList;
    }

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
