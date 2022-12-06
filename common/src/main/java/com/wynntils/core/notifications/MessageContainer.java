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
        this.message = new TextRenderTask(message, TextRenderSetting.DEFAULT);
        this.messageCount = 0;
    }

    public MessageContainer(TextRenderTask message) {
        this.message = message;
        this.messageCount = 0;
    }

    public TextRenderTask getRenderTask() {
        String sendableMessage = message.getText() + duplicateMessageBuilder(this);
        return new TextRenderTask(sendableMessage, this.message.getSetting());
    }

    public Integer getMessageCount() {
        return messageCount;
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

    private String duplicateMessageBuilder(MessageContainer container) {
        // We don't want to send append the duplicate count to the message if it's not actually a dupe.
        if (this.messageCount <= 1) return "";
        return " §7[x" + this.messageCount + "]";
    }
}
