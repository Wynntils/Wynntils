/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.utils.mc.type.CodedString;
import net.minecraftforge.eventbus.api.Event;

/** Fires on change to footer of scoreboard */
public class PlayerInfoFooterChangedEvent extends Event {
    private final CodedString footer;

    public CodedString getFooter() {
        return footer;
    }

    public PlayerInfoFooterChangedEvent(CodedString footer) {
        this.footer = footer;
    }
}
