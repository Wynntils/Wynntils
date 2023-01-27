/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.concepts.Element;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
import java.util.function.Consumer;

public final class DefenceStatBuilder extends StatBuilder {
    @Override
    public void buildStats(Consumer<StatType> callback) {
        for (Element element : Element.values()) {
            // The difference in spelling (defence/defense) is due to Wynncraft. Do not change.
            StatType statType = new StatType(
                    "DEFENCE_" + element.name(),
                    element.getDisplayName() + " Defence",
                    "bonus" + element.getDisplayName() + "Defense",
                    element.name() + "DEFENSE",
                    StatUnit.PERCENT);
            callback.accept(statType);
        }
    }
}
