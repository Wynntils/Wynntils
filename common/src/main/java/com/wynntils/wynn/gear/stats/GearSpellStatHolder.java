package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;
import com.wynntils.wynn.objects.SpellType;

public class GearSpellStatHolder extends GearStatHolder {
    private final SpellType spellType;
    GearSpellStatHolder(SpellType spellType, String key, String displayName, String apiName, String loreName, GearStatUnit unit) {
        super(key, displayName, apiName, loreName, unit);
        this.spellType = spellType;
    }
}
