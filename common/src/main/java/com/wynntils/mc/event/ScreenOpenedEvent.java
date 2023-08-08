/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Fired on setting the active screen */
public abstract class ScreenOpenedEvent extends Event {
    private final Screen screen;

    protected ScreenOpenedEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    @Cancelable
    public static class Pre extends ScreenOpenedEvent {
        public Pre(Screen screen) {
            super(screen);
        }
    }

    // NOTE: This event is not actually cancelable, but it is marked as such to make higher priority listeners cancel
    // the event for lower priorities
    @Cancelable
    public static class Post extends ScreenOpenedEvent {
        public Post(Screen screen) {
            super(screen);
        }
    }
}
