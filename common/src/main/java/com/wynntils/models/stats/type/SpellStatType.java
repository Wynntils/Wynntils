/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.models.spells.type.SpellType;

public final class SpellStatType extends StatType {
    private final SpellType spellType;

    public SpellStatType(
            String key,
            String displayName,
            String apiName,
            String internalRollName,
            StatUnit unit,
            SpellType spellType) {
        super(key, displayName, apiName, internalRollName, unit);
        this.spellType = spellType;
    }

    public SpellType getSpellType() {
        return spellType;
    }

    @Override
    public boolean displayAsInverted() {
        // Negative spell stats are positive for the player
        return true;
    }

    @Override
    public boolean calculateAsInverted() {
        // Spell costs are calculated using inverted values to account for rounding
        return true;
    }
}
