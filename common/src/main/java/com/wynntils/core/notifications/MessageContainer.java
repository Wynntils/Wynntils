/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;

public class MessageContainer {
    TextRenderTask message;

    public MessageContainer(String message) {
        this.message = new TextRenderTask(message, TextRenderSetting.DEFAULT);
    }

    public MessageContainer(TextRenderTask message) {
        this.message = message;
    }

    public TextRenderTask getRenderTask() {
        return message;
    }

    public MessageContainer editMessage(String newMessage) {
        this.message.setText(newMessage);
        return this;
    }

    public void update(MessageContainer other) {
        this.message = other.message;
    }
}
