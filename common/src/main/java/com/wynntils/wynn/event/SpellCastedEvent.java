/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import com.wynntils.wynn.objects.SpellType;
import net.minecraftforge.eventbus.api.Event;

public class SpellCastedEvent extends Event {

    private final SpellType spell;

    public SpellCastedEvent(SpellType spell) {
        this.spell = spell;
    }

    public SpellType getSpell() {
        return spell;
    }
}
