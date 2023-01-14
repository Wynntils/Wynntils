/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.gear;

import com.wynntils.wynn.objects.SpellType;
import java.util.ArrayList;
import java.util.List;

public class IdSpellTypes implements IdType {
    public static final List<IdSpellTypes> spellTypeIds = new ArrayList<>();

    private final SpellType spellType;
    private final String displayName;
    private final String unit;
    private final String athenaName;
    private final String loreName;
    private final String apiName;

    static {
        generate();
    }

    IdSpellTypes(
            SpellType spellType, String displayName, String unit, String athenaName, String loreName, String apiName) {
        this.spellType = spellType;
        this.displayName = displayName;
        this.unit = unit;
        this.athenaName = athenaName;
        this.loreName = loreName;
        this.apiName = apiName;
    }

    private static void generate() {
        // can also be "§a+1§r§7 {sp1} Cost"
        for (var spellType : SpellType.values()) {
            String ordinal =
                    switch (spellType.getSpellNumber()) {
                        case 1 -> "1st";
                        case 2 -> "2nd";
                        case 3 -> "3rd";
                        case 4 -> "4th";
                        default -> throw new IllegalStateException("Bad SpellType");
                    };
            String athenaName = ordinal + "SpellCost";
            String displayName = spellType.getName() + " Cost";
            // FIXME: figure out lore and api name

    /*

    "SPELL_COST_RAW_1" -> "raw1stSpellCost"
    "SPELL_COST_RAW_2" -> "raw2ndSpellCost"
    "SPELL_COST_RAW_3" -> "raw3rdSpellCost"
    "SPELL_COST_RAW_4" -> "raw4thSpellCost"
    "SPELL_COST_PCT_1" -> "1stSpellCost"
    "SPELL_COST_PCT_2" -> "2ndSpellCost"
    "SPELL_COST_PCT_3" -> "3rdSpellCost"
    "SPELL_COST_PCT_4" -> "4thSpellCost"
     */


            IdSpellTypes percentType = new IdSpellTypes(spellType, displayName, "%", athenaName, null, null);
            spellTypeIds.add(percentType);
            IdSpellTypes rawType = new IdSpellTypes(spellType, displayName, null, "raw" + athenaName, null, null);
            spellTypeIds.add(rawType);
        }
    }

    @Override
    public String getKey() {
        if (unit == null) {
            return spellType.name() + "_COST_RAW";
        } else {
            return spellType.name() + "_COST_PERCENT";
        }
    }

    @Override
    public IsVariable getIsVariable() {
        // spellCostRaw4 == variable!
        return IsVariable.UNKNOWN;
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
