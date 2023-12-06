/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
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
            SpellType spellType = SpellType.forClass(ClassType.NONE, spellNumber);

            callback.accept(buildSpellStat(spellType, StatUnit.PERCENT));
            callback.accept(buildSpellStat(spellType, StatUnit.RAW));
        }
    }

    public static String getStatNameForSpell(String spellName) {
        return spellName + " Cost";
    }

    public static List<String> getAliases(SpellStatType statType) {
        List<String> aliases = new ArrayList<>();
        SpellType genericSpell = statType.getSpellType();
        for (ClassType classType : ClassType.values()) {
            // Skip the unspecified class, we have that as our base name
            if (classType == ClassType.NONE) continue;

            SpellType classSpecificSpell = genericSpell.forOtherClass(classType);
            aliases.add(getStatNameForSpell(classSpecificSpell.getName()));
        }
        // Also add an alias of the form "{sp1} Cost" which can appear on unidentified gear
        String unidentifiedAliasName = getStatNameForSpell("{sp" + genericSpell.getSpellNumber() + "}");
        aliases.add(unidentifiedAliasName);

        return Collections.unmodifiableList(aliases);
    }

    private SpellStatType buildSpellStat(SpellType spellType, StatUnit unit) {
        String apiUnit = unit == StatUnit.RAW ? "raw" : "";
        String loreUnit = apiUnit.toUpperCase(Locale.ROOT);
        int spellNumber = spellType.getSpellNumber();
        String spellNumberString =
                switch (spellNumber) {
                    case 1 -> "1st";
                    case 2 -> "2nd";
                    case 3 -> "3rd";
                    case 4 -> "4th";
                    default -> throw new IllegalArgumentException("Invalid spell number: " + spellNumber);
                };

        return new SpellStatType(
                "SPELL_" + spellType.name() + "_COST_" + unit.name(),
                getStatNameForSpell(spellType.getName()),
                apiUnit + spellNumberString + "SpellCost",
                "SPELL_COST_" + loreUnit + "_" + spellNumber,
                unit,
                spellType);
    }
}
