/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.gear;

import com.wynntils.wynn.objects.SpellType;
import java.util.ArrayList;
import java.util.List;

public class IdSpellTypes {
    public static final List<IdSpellTypes> spellTypeIds = new ArrayList<>();

    private final String name;
    private final String unit;
    private final String athenaName;
    private final String loreName;
    private final String apiName;

    static {
        generate();
    }

    IdSpellTypes(String name, String unit, String athenaName, String loreName, String apiName) {
        this.name = name;
        this.unit = unit;
        this.athenaName = athenaName;
        this.loreName = loreName;
        this.apiName = apiName;
    }

    private static void generate() {
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
            String name = spellType.getName();
            // FIXME: figure out lore and api name
            IdSpellTypes percentType = new IdSpellTypes(name, "%", athenaName, null, null);
            spellTypeIds.add(percentType);
            IdSpellTypes rawType = new IdSpellTypes(name, null, "raw" + athenaName, null, null);
            spellTypeIds.add(rawType);
        }
    }
}
