/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.stats.type.DefenceStatType;
import com.wynntils.models.stats.type.StatUnit;
import com.wynntils.utils.StringUtils;
import java.util.function.Consumer;

public final class DefenceStatBuilder extends StatBuilder<DefenceStatType> {
    @Override
    public void buildStats(Consumer<DefenceStatType> callback) {
        for (Element element : Element.values()) {
            // The difference in spelling (defence/defense) is due to Wynncraft. Do not change.
            DefenceStatType statType = new DefenceStatType(
                    "DEFENCE_" + element.name(),
                    element.getDisplayName() + " Defence",
                    StringUtils.uncapitalizeFirst(element.getDisplayName()) + "Defence",
                    element.name() + "DEFENSE",
                    StatUnit.PERCENT);
            callback.accept(statType);
        }

        // Both of these stats are percentages
        callback.accept(new DefenceStatType(
                "DEFENCE_ELEMENTAL", "Elemental Defense", "elementalDefense", "ELEMENTALDEFENSE", StatUnit.PERCENT));
        callback.accept(new DefenceStatType(
                "DEFENCE_ELEMENTAL_BONUS",
                "Bonus Elemental Defense",
                "bonusElementalDefense",
                "ELEMENTALDEFENSEBONUS",
                StatUnit.PERCENT));
    }
}
