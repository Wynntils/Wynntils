/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.events;

import net.minecraftforge.eventbus.api.Event;

public class WynntilsEvent extends Event {
    private boolean posted = false;

    public boolean wasPosted() {
        return posted;
    }

    // doesn't take an argument because you can't "unpost" an event
    public void setPosted() {
        posted = true;
    }
}
