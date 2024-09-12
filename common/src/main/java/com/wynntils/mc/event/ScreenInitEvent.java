/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.Event;

/**
 * Fired when a screen is re-inited. Use this to add widgets.
 */
public abstract class ScreenInitEvent extends Event {
    private final Screen screen;
    private final boolean firstInit;

    protected ScreenInitEvent(Screen screen, boolean firstInit) {
        this.screen = screen;
        this.firstInit = firstInit;
    }

    public Screen getScreen() {
        return screen;
    }

    public boolean isFirstInit() {
        return firstInit;
    }

    public static class Pre extends ScreenInitEvent {
        public Pre(Screen screen, boolean firstInit) {
            super(screen, firstInit);
        }
    }

    public static class Post extends ScreenInitEvent {
        public Post(Screen screen, boolean firstInit) {
            super(screen, firstInit);
        }
    }
}
