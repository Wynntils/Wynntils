/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.client.gui.screens.Screen;

/**
 * Fired when a screen is re-inited. Use this to add widgets.
 */
public abstract class ScreenInitEvent extends BaseEvent {
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

    public static final class Pre extends ScreenInitEvent {
        public Pre(Screen screen, boolean firstInit) {
            super(screen, firstInit);
        }
    }

    public static final class Post extends ScreenInitEvent {
        public Post(Screen screen, boolean firstInit) {
            super(screen, firstInit);
        }
    }
}
