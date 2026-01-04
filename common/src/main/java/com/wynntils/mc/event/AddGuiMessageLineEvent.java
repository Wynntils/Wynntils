/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.GuiMessage;
import net.neoforged.bus.api.Event;

public class AddGuiMessageLineEvent extends Event {
    private final GuiMessage message;
    private final GuiMessage.Line line;

    public AddGuiMessageLineEvent(GuiMessage message, GuiMessage.Line line) {
        this.message = message;
        this.line = line;
    }

    public GuiMessage getMessage() {
        return message;
    }

    public GuiMessage.Line getLine() {
        return line;
    }
}
