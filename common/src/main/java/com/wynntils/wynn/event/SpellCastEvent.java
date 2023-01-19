/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import com.wynntils.wynn.objects.SpellDirection;
import com.wynntils.wynn.objects.SpellType;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired upon successful, completed spell cast.
 * FIXME: This event fires upon every three-click sequence, no matter if the spell was actually casted or not.
 * FIXME: This event should be checking if the cast failed due to mana/unlock restrictions.
 */
public class SpellCastEvent extends SpellEvent {

    private final SpellType spell;

    public SpellCastEvent(SpellDirection[] spellDirectionArray, Source source, SpellType spell) {
        super(spellDirectionArray, source);
        this.spell = spell;
    }

    public SpellType getSpell() {
        return spell;
    }
}
