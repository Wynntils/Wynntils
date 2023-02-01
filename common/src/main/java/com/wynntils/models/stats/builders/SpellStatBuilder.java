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
        List aliases = new ArrayList<>();
        SpellType spellType = statType.getSpellType();
        for (ClassType classType : ClassType.values()) {
            // Skip the unspecified class, we have that as our base name
            if (classType == ClassType.None) continue;

            SpellType spell = spellType.forOtherClass(classType);
            aliases.add(getStatNameFromSpell(spell.getName()));
        }
        // Also add an alias of the form "{sp1} Cost" which can appear on unidentified gear
        String unidentifiedAliasName = getStatNameFromSpell("{sp" + spellType.getSpellNumber() + "}");
        aliases.add(unidentifiedAliasName);

        return Collections.unmodifiableList(aliases);
    }

    private SpellStatType buildSpellStat(SpellType spellType, StatUnit unit) {
        String apiUnit = (unit == StatUnit.RAW) ? "Raw" : "Pct";
        String loreUnit = apiUnit.toUpperCase(Locale.ROOT);
        int spellNumber = spellType.getSpellNumber();

        return new SpellStatType(
                "SPELL_" + spellType.name() + "_COST_" + unit.name(),
                getStatNameFromSpell(spellType.getName()),
                "spellCost" + apiUnit + spellNumber,
                "SPELL_COST_" + loreUnit + "_" + spellNumber,
                unit,
                spellType);
    }

    public static String getStatNameFromSpell(String spellName) {
        return spellName + " Cost";
    }
}
