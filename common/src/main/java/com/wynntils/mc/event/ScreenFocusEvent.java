/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

/**
 * Fired when the screen changes its focused widget.
 */
public class ScreenFocusEvent extends BaseEvent implements CancelRequestable {
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
