/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraft.client.gui.screens.Screen;

/** Fired on setting the active screen */
public class ScreenOpenedEvent extends WynntilsEvent {
    private final Screen screen;

    public ScreenOpenedEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }
}
