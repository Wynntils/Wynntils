/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

// FIXME: I need a better way to think about Spell Stats!!!
// Can've have them multiply all over the place. They need aliases?
// And a way to present themselves differently depending on item type.
public final class SpellStatType extends StatType {
    public SpellStatType(String key, String displayName, String apiName, String internalRollName, StatUnit unit) {
        super(key, displayName, apiName, internalRollName, unit);
    }

    @Override
    public boolean showAsInverted() {
        // Note that this is used only when displaying; internally the value is
        // represented as positive (> 0).
        return true;
    }
}
