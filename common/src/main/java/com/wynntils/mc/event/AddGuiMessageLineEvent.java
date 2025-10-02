/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.client.GuiMessage;

public class AddGuiMessageLineEvent extends BaseEvent {
    private final GuiMessage message;
    private final GuiMessage.Line line;
    private final int index;

    public AddGuiMessageLineEvent(GuiMessage message, GuiMessage.Line line, int index) {
        this.message = message;
        this.line = line;
        this.index = index;
    }

    public GuiMessage getMessage() {
        return message;
    }

    public GuiMessage.Line getLine() {
        return line;
    }

    public int getIndex() {
        return index;
    }
}
