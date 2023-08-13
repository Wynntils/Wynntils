/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when the screen changes its focused widget.
 */
@Cancelable
public class ScreenFocusEvent extends Event {
    private final Screen screen;
    private final GuiEventListener guiEventListener;

    public ScreenFocusEvent(Screen screen, GuiEventListener guiEventListener) {
        this.screen = screen;
        this.guiEventListener = guiEventListener;
    }

    public Screen getScreen() {
        return screen;
    }

    public GuiEventListener getGuiEventListener() {
        return guiEventListener;
    }
}
