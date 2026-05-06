/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.bus.api.Event;

/** Fired when an existing {@link TitleScreen} rebuilds its widgets, for example after a resize. */
public abstract class TitleScreenRebuildEvent extends Event {
    private final TitleScreen titleScreen;

    protected TitleScreenRebuildEvent(TitleScreen titleScreen) {
        this.titleScreen = titleScreen;
    }

    public TitleScreen getTitleScreen() {
        return titleScreen;
    }

    public static final class Pre extends TitleScreenRebuildEvent {
        public Pre(TitleScreen titleScreen) {
            super(titleScreen);
        }
    }

    public static final class Post extends TitleScreenRebuildEvent {
        public Post(TitleScreen titleScreen) {
            super(titleScreen);
        }
    }
}
