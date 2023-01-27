/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
import java.util.Locale;
import java.util.function.Consumer;

public final class SpellStatBuilder extends StatBuilder {
    @Override
    public void buildStats(Consumer<StatType> callback) {
        for (SpellType spellType : SpellType.values()) {
            int spellNumber = spellType.getSpellNumber();
            String displayName = spellType.getName() + " Cost";

            StatType percentType = buildSpellStat(spellType, spellNumber, displayName, StatUnit.PERCENT, "");
            callback.accept(percentType);

            StatType rawType = buildSpellStat(spellType, spellNumber, displayName, StatUnit.RAW, "");
            callback.accept(rawType);

            if (spellType.getClassType() == ClassType.None) {
                // Also add an alias of the form "{sp1} Cost" which can appear on Unidentified gear
                String aliasName = "{sp" + spellNumber + "} Cost";
                StatType percentTypeAlias =
                        buildSpellStat(spellType, spellNumber, aliasName, StatUnit.PERCENT, "_ALIAS");
                callback.accept(percentTypeAlias);

                StatType rawTypeAlias = buildSpellStat(spellType, spellNumber, aliasName, StatUnit.RAW, "_ALIAS");
                callback.accept(rawTypeAlias);
            }
        }
    }

    private StatType buildSpellStat(
            SpellType spellType, int spellNumber, String displayName, StatUnit unit, String postfix) {
        String apiUnit = (unit == StatUnit.RAW) ? "Raw" : "Pct";
        String loreUnit = apiUnit.toUpperCase(Locale.ROOT);

        return new StatType(
                "SPELL_" + spellType.name() + "_COST_" + unit.name() + postfix,
                displayName,
                "spellCost" + apiUnit + spellNumber,
                "SPELL_COST_" + loreUnit + "_" + spellNumber,
                unit);
    }
}
