/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public class MouseScrollEvent extends Event {

    private final boolean isScrollingUp;

    public MouseScrollEvent(boolean isScrollingUp) {
        this.isScrollingUp = isScrollingUp;
    }

    public boolean isScrollingUp() {
        return isScrollingUp;
    }
}
