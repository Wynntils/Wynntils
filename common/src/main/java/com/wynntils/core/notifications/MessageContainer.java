/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;

public class MessageContainer {
    protected TextRenderTask message;
    private TextRenderTask renderedMessage;
    private int messageCount;

    public MessageContainer(String message) {
        this.message = new TextRenderTask(message, TextRenderSetting.DEFAULT);
    }

    public MessageContainer(TextRenderTask message) {
        this.message = message;
        this.messageCount = 1;
    }

    public String getOriginalMessage() {
        return message.getText();
    }

    public TextRenderTask getRenderTask() {
        if (renderedMessage == null) return message;
        return renderedMessage;
    }

    public int getMessageCount() {
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
        this.renderedMessage =
                new TextRenderTask(this.message.getText() + duplicateMessageBuilder(this), this.message.getSetting());
    }

    private String duplicateMessageBuilder(MessageContainer container) {
        // We don't want to append the duplicate count to the message if it's not actually a dupe.
        if (this.messageCount <= 1) return "";
        return " §7[x" + this.messageCount + "]";
    }
}
