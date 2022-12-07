/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;

public class MessageContainer {
    protected TextRenderTask message;
    private int messageCount;

    public MessageContainer(String message) {
        this(new TextRenderTask(message, TextRenderSetting.DEFAULT));
    }

    public MessageContainer(TextRenderTask message) {
        this.message = message;
        this.messageCount = 1;
    }

    public String getOriginalMessage() {
        return message.getText();
    }

    public TextRenderTask getRenderTask() {
        if (this.messageCount == 1) {
            return message;
        }

        String messageMultiplier = " §7[x" + this.messageCount + "]";
        return new TextRenderTask(this.message.getText() + messageMultiplier, this.message.getSetting());
    }

    public void editMessage(String newMessage) {
        this.message.setText(newMessage);
    }

    public void update(MessageContainer other) {
        this.message = other.message;
    }

    public void incrementMessageCount() {
        this.messageCount++;
    }
}
