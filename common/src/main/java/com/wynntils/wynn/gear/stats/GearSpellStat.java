/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.objects.ClassType;
import com.wynntils.wynn.objects.SpellType;
import java.util.ArrayList;
import java.util.List;

public class GearSpellStat implements GearStat {
    public static final List<GearSpellStat> spellTypeIds = new ArrayList<>();

    private final SpellType spellType;
    private final String key;
    private final String displayName;
    private final String unit;
    private final String athenaName;
    private final String loreName;
    private final String apiName;

    static {
        generate();
    }

    GearSpellStat(
            SpellType spellType,
            String key,
            String displayName,
            String unit,
            String athenaName,
            String loreName,
            String apiName) {
        this.spellType = spellType;
        this.key = key;
        this.displayName = displayName;
        this.unit = unit;
        this.athenaName = athenaName;
        this.loreName = loreName;
        this.apiName = apiName;
    }

    private static void generate() {
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
            String athenaName = ordinal + "SpellCost";
            String displayName = spellType.getName() + " Cost";

            GearSpellStat percentType = new GearSpellStat(
                    spellType,
                    spellType.name() + "_COST_PERCENT",
                    displayName,
                    "%",
                    athenaName,
                    "SPELL_COST_PCT_" + spellNumber,
                    "spellCostPct" + spellNumber);
            spellTypeIds.add(percentType);
            GearSpellStat rawType = new GearSpellStat(
                    spellType,
                    spellType.name() + "_COST_RAW",
                    displayName,
                    null,
                    "raw" + athenaName,
                    "SPELL_COST_RAW_" + spellNumber,
                    "spellCostRaw" + spellNumber);
            spellTypeIds.add(rawType);
            if (spellType.getClassType() == ClassType.None) {
                // Also add an alias of the form "{sp1} Cost" which can appear on Unidentified gear
                GearSpellStat rawTypeAlias = new GearSpellStat(
                        spellType,
                        spellType.name() + "_COST_RAW_ALIAS",
                        "{sp" + spellNumber + "} Cost",
                        null,
                        "raw" + athenaName,
                        "SPELL_COST_RAW_" + spellNumber,
                        "spellCostRaw" + spellNumber);
                spellTypeIds.add(rawTypeAlias);
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
    public String getUnit() {
        return unit;
    }

    @Override
    public String getAthenaName() {
        return athenaName;
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
