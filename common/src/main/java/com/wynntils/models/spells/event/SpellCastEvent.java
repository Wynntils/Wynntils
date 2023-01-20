/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.event;

import com.wynntils.models.spells.type.SpellType;
import net.minecraftforge.eventbus.api.Event;

public class SpellCastEvent extends Event {

    private final SpellType spell;

    public SpellCastEvent(SpellType spell) {
        this.spell = spell;
    }

    public SpellType getSpell() {
        return spell;
    }
}
