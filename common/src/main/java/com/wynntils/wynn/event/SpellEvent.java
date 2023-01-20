/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import com.wynntils.wynn.objects.SpellDirection;
import net.minecraftforge.eventbus.api.Event;

public abstract class SpellEvent extends Event {
    private final SpellDirection[] spellDirectionArray;
    private final Source source;

    protected SpellEvent(SpellDirection[] spellDirectionArray, Source source) {
        this.spellDirectionArray = spellDirectionArray;
        this.source = source;
    }

    public SpellDirection[] getSpellDirectionArray() {
        return spellDirectionArray.clone();
    }

    public Source getSource() {
        return source;
    }

    public enum Source {
        HOTBAR,
        TITLE_LETTER,
        TITLE_FULL;
    }
}
