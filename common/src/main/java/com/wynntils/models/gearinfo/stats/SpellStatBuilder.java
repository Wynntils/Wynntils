/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.stats;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gearinfo.types.GearStat;
import com.wynntils.models.gearinfo.types.GearStatUnit;
import com.wynntils.models.spells.type.SpellType;
import java.util.Locale;
import java.util.function.Consumer;

public final class SpellStatBuilder extends StatBuilder {
    @Override
    public void buildStats(Consumer<GearStat> callback) {
        for (SpellType spellType : SpellType.values()) {
            int spellNumber = spellType.getSpellNumber();
            String displayName = spellType.getName() + " Cost";

            GearStat percentType = buildSpellStat(spellType, spellNumber, displayName, GearStatUnit.PERCENT, "");
            callback.accept(percentType);

            GearStat rawType = buildSpellStat(spellType, spellNumber, displayName, GearStatUnit.RAW, "");
            callback.accept(rawType);

            if (spellType.getClassType() == ClassType.None) {
                // Also add an alias of the form "{sp1} Cost" which can appear on Unidentified gear
                String aliasName = "{sp" + spellNumber + "} Cost";
                GearStat percentTypeAlias =
                        buildSpellStat(spellType, spellNumber, aliasName, GearStatUnit.PERCENT, "_ALIAS");
                callback.accept(percentTypeAlias);

                GearStat rawTypeAlias = buildSpellStat(spellType, spellNumber, aliasName, GearStatUnit.RAW, "_ALIAS");
                callback.accept(rawTypeAlias);
            }
        }
    }

    private GearStat buildSpellStat(
            SpellType spellType, int spellNumber, String displayName, GearStatUnit unit, String postfix) {
        String apiUnit = (unit == GearStatUnit.RAW) ? "Raw" : "Pct";
        String loreUnit = apiUnit.toUpperCase(Locale.ROOT);

        return new GearStat(
                "SPELL_" + spellType.name() + "_COST_" + unit.name() + postfix,
                displayName,
                "spellCost" + apiUnit + spellNumber,
                "SPELL_COST_" + loreUnit + "_" + spellNumber,
                unit);
    }
}
