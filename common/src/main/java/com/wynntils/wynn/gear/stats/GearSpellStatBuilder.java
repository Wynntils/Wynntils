/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;
import com.wynntils.wynn.objects.ClassType;
import com.wynntils.wynn.objects.SpellType;
import java.util.List;

public class GearSpellStatBuilder {
    public static void addStats(List<GearStat> registry) {
        for (var spellType : SpellType.values()) {
            int spellNumber = spellType.getSpellNumber();
            String ordinal =
                    switch (spellNumber) {
                        case 1 -> "1st";
                        case 2 -> "2nd";
                        case 3 -> "3rd";
                        case 4 -> "4th";
                        default -> throw new IllegalStateException("Bad SpellType");
                    };
            String displayName = spellType.getName() + " Cost";

            GearSpellStatHolder percentType = new GearSpellStatHolder(
                    spellType,
                    "SPELL_" + spellType.name() + "_COST_PERCENT",
                    displayName,
                    "spellCostPct" + spellNumber,
                    "SPELL_COST_PCT_" + spellNumber,
                    GearStatUnit.PERCENT);
            registry.add(percentType);
            GearSpellStatHolder rawType = new GearSpellStatHolder(
                    spellType,
                    "SPELL_" + spellType.name() + "_COST_RAW",
                    displayName,
                    "spellCostRaw" + spellNumber,
                    "SPELL_COST_RAW_" + spellNumber,
                    GearStatUnit.RAW);
            registry.add(rawType);
            if (spellType.getClassType() == ClassType.None) {
                // Also add an alias of the form "{sp1} Cost" which can appear on Unidentified gear
                GearSpellStatHolder rawTypeAlias = new GearSpellStatHolder(
                        spellType,
                        spellType.name() + "_COST_RAW_ALIAS",
                        "{sp" + spellNumber + "} Cost",
                        "spellCostRaw" + spellNumber,
                        "SPELL_COST_RAW_" + spellNumber,
                        null);
                registry.add(rawTypeAlias);
            }
        }
    }
}
