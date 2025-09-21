/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Fired on setting the active screen */
public abstract class ScreenOpenedEvent extends Event {
    private final Screen screen;

    protected ScreenOpenedEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    public static class Pre extends ScreenOpenedEvent implements ICancellableEvent {
        private final Screen oldScreen;

        public Pre(Screen screen, Screen oldScreen) {
            super(screen);
            this.oldScreen = oldScreen;
        }

        public Screen getOldScreen() {
            return oldScreen;
        }
    }

    // NOTE: This event is not actually cancelable, but it is marked as such to make higher priority listeners cancel
    //       the event for lower priorities
    public static class Post extends ScreenOpenedEvent implements ICancellableEvent {
        public Post(Screen screen) {
            super(screen);
        }
    }
}
