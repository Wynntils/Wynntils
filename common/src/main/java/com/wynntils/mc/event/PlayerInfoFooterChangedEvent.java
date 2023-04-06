/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.text.StyledText2;
import net.minecraftforge.eventbus.api.Event;

/** Fires on change to footer of scoreboard */
public class PlayerInfoFooterChangedEvent extends Event {
    private final StyledText2 footer;

    public StyledText2 getFooter() {
        return footer;
    }

    public PlayerInfoFooterChangedEvent(StyledText2 footer) {
        this.footer = footer;
    }
}
