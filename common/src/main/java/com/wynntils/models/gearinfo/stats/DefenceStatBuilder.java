/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.stats;

import com.wynntils.models.concepts.Element;
import com.wynntils.models.gearinfo.types.GearStat;
import com.wynntils.models.gearinfo.types.GearStatUnit;
import java.util.function.Consumer;

public final class DefenceStatBuilder extends StatBuilder {
    @Override
    public void buildStats(Consumer<GearStat> callback) {
        for (Element element : Element.values()) {
            // The difference in spelling (defence/defense) is due to Wynncraft. Do not change.
            GearStat gearStat = new GearStat(
                    "DEFENCE_" + element.name(),
                    element.getDisplayName() + " Defence",
                    "bonus" + element.getDisplayName() + "Defense",
                    element.name() + "DEFENSE",
                    GearStatUnit.PERCENT);
            callback.accept(gearStat);
        }
    }
}
