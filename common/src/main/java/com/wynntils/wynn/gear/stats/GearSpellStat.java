/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;
import com.wynntils.wynn.objects.SpellType;

public class GearSpellStat extends GearStat {
    private final SpellType spellType;

    GearSpellStat(
            SpellType spellType, String key, String displayName, String apiName, String loreName, GearStatUnit unit) {
        super(key, displayName, apiName, loreName, unit);
        this.spellType = spellType;
    }
}
