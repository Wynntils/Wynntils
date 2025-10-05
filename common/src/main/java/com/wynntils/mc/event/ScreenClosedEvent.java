/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class ScreenClosedEvent extends Event {
    private final Screen screen;

    protected ScreenClosedEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    public static final class Pre extends ScreenClosedEvent implements ICancellableEvent {
        public Pre(Screen screen) {
            super(screen);
        }
    }

    public static final class Post extends ScreenClosedEvent {
        public Post(Screen screen) {
            super(screen);
        }
    }
}
