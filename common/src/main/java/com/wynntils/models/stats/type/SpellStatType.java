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
    public boolean showAsInverted() {
        // Note that this is used only when displaying; internally the value is
        // represented as positive (> 0).
        return true;
    }
}
