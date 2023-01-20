/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import com.wynntils.wynn.objects.SpellDirection;

/**
 * Fired upon user inputting the next click in a sequence to cast a spell.
 */
public class SpellProgressEvent extends SpellEvent {

    public SpellProgressEvent(SpellDirection[] spellDirectionArray, Source source) {
        super(spellDirectionArray, source);
    }
}
