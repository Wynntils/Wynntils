/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;
import com.wynntils.wynn.objects.ClassType;
import com.wynntils.wynn.objects.SpellType;
import java.util.List;

public class GearSpellStatBuilder implements GearStat {
    private final SpellType spellType;
    private final String key;
    private final String displayName;
    private final GearStatUnit unit;
    private final String loreName;
    private final String apiName;

    GearSpellStatBuilder(SpellType spellType, String key, String displayName, GearStatUnit unit, String loreName, String apiName) {
        this.spellType = spellType;
        this.key = key;
        this.displayName = displayName;
        this.unit = unit;
        this.loreName = loreName;
        this.apiName = apiName;
    }

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

            GearSpellStatBuilder percentType = new GearSpellStatBuilder(
                    spellType,
                    "SPELL_" + spellType.name() + "_COST_PERCENT",
                    displayName,
                    GearStatUnit.PERCENT,
                    "SPELL_COST_PCT_" + spellNumber,
                    "spellCostPct" + spellNumber);
            registry.add(percentType);
            GearSpellStatBuilder rawType = new GearSpellStatBuilder(
                    spellType,
                    "SPELL_" + spellType.name() + "_COST_RAW",
                    displayName,
                    GearStatUnit.RAW,
                    "SPELL_COST_RAW_" + spellNumber,
                    "spellCostRaw" + spellNumber);
            registry.add(rawType);
            if (spellType.getClassType() == ClassType.None) {
                // Also add an alias of the form "{sp1} Cost" which can appear on Unidentified gear
                GearSpellStatBuilder rawTypeAlias = new GearSpellStatBuilder(
                        spellType,
                        spellType.name() + "_COST_RAW_ALIAS",
                        "{sp" + spellNumber + "} Cost",
                        null,
                        "SPELL_COST_RAW_" + spellNumber,
                        "spellCostRaw" + spellNumber);
                registry.add(rawTypeAlias);
            }
        }
    }



    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public GearStatUnit getUnit() {
        return unit;
    }

    @Override
    public String getLoreName() {
        return loreName;
    }

    @Override
    public String getApiName() {
        return apiName;
    }
}
