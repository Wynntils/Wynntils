/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

/** Fires on change to footer of scoreboard */
public class PlayerInfoFooterChangedEvent extends Event {
    private final String footer;

    public String getFooter() {
        return footer;
    }

    public PlayerInfoFooterChangedEvent(String footer) {
        this.footer = footer;
    }
}
