/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.eventbus.api.Event;

public class ScreenOpenedEvent extends Event {
    private final Screen screen;

    public ScreenOpenedEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }
}
