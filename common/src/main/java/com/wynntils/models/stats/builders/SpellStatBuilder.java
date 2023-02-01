/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.models.stats.type.SpellStatType;
import com.wynntils.models.stats.type.StatUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public final class SpellStatBuilder extends StatBuilder<SpellStatType> {
    @Override
    public void buildStats(Consumer<SpellStatType> callback) {
        for (int spellNumber = 1; spellNumber <= SpellType.MAX_SPELL; spellNumber++) {
            SpellType spellType = SpellType.forClass(ClassType.None, spellNumber);

            callback.accept(buildSpellStat(spellType, StatUnit.PERCENT));
            callback.accept(buildSpellStat(spellType, StatUnit.RAW));
        }
    }

    public static List<String> getAliases(SpellStatType statType) {
        ArrayList aliases = new ArrayList<>();
        int spellNumber = statType.getSpellNumber();
        for (ClassType classType : ClassType.values()) {
            SpellType spell = SpellType.forClass(classType, spellNumber);
            aliases.add(getName(spell));
        }
        // Also add an alias of the form "{sp1} Cost" which can appear on unidentified gear
        String unidentifiedAliasName = "{sp" + spellNumber + "} Cost";
        aliases.add(unidentifiedAliasName);

        return Collections.unmodifiableList(aliases);
    }

    private SpellStatType buildSpellStat(SpellType spellType, StatUnit unit) {
        String apiUnit = (unit == StatUnit.RAW) ? "Raw" : "Pct";
        String loreUnit = apiUnit.toUpperCase(Locale.ROOT);
        int spellNumber = spellType.getSpellNumber();

        return new SpellStatType(
                "SPELL_" + spellType.name() + "_COST_" + unit.name(),
                getName(spellType),
                "spellCost" + apiUnit + spellNumber,
                "SPELL_COST_" + loreUnit + "_" + spellNumber,
                unit,
                spellNumber);
    }

    private static String getName(SpellType spellType) {
        return spellType.getName() + " Cost";
    }
}
