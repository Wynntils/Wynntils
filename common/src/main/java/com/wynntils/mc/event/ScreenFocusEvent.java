/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when the screen changes its focused widget.
 */
public class ScreenFocusEvent extends BaseEvent implements ICancellableEvent {
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
