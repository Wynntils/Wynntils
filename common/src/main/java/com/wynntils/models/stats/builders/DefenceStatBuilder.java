/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.stats.type.DefenceStatType;
import com.wynntils.models.stats.type.StatType;
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

        callback.accept(new DefenceStatType(
                "DEFENCE_ELEMENTAL", "Elemental Defence", "elementalDefence", "ELEMENTAL_DEFENSE", StatUnit.PERCENT));

        // Special case for the "defenceToMobs" tome stat
        callback.accept(new DefenceStatType(
                "DEFENCE_TO_MOBS",
                "Mob Damage Resistance",
                "defenceToMobs",
                "DEFENCETOMOBS",
                StatUnit.PERCENT,
                StatType.SpecialStatType.TOME_BASE_STAT,
                false));

        // Special case for "damageFromMobs" charm stat
        // "Damage from mobs" is technically a defence stat, not a damage stat
        callback.accept(new DefenceStatType(
                "DAMAGE_FROM_MOBS",
                "Damage taken from mobs",
                "damageFromMobs",
                "DAMAGEFROMMOBS",
                StatUnit.PERCENT,
                StatType.SpecialStatType.CHARM_LEVELED_STAT,
                true));
    }
}
