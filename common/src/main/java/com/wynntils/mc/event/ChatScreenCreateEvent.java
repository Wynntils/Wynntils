/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.Event;

public class ChatScreenCreateEvent extends Event {
    private Screen screen;
    private final String defaultText;
    private final boolean isDraft;

    public ChatScreenCreateEvent(Screen screen, String defaultText, boolean isDraft) {
        this.screen = screen;
        this.defaultText = defaultText;
        this.isDraft = isDraft;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public boolean isDraft() {
        return isDraft;
    }
}
