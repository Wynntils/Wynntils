/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraft.client.gui.screens.Screen;

/**
 * Fired when a screen is re-inited. Use this to add widgets.
 */
public class ScreenInitEvent extends WynntilsEvent {
    private final Screen screen;

    public ScreenInitEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }
}
