/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.event;

import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.spells.type.SpellType;

/**
 * Fired upon successful, completed spell cast.
 * FIXME: This event fires upon every three-click sequence, no matter if the spell was actually casted or not.
 * FIXME: This event should be checking if the cast failed due to mana/unlock restrictions.
 */
public class TrySpellCastEvent extends SpellEvent {

    private final SpellType spell;

    public TrySpellCastEvent(SpellDirection[] spellDirectionArray, Source source, SpellType spell) {
        super(spellDirectionArray, source);
        this.spell = spell;
    }

    public SpellType getSpell() {
        return spell;
    }
}
