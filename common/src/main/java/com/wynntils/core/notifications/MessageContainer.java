/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import java.util.Objects;

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

    public String getMessage() {
        return message.getText();
    }

    public TextRenderTask getRenderTask() {
        if (this.messageCount == 1) {
            return message;
        }

        String messageMultiplier = " §7[x" + this.messageCount + "]";
        return new TextRenderTask(this.message.getText() + messageMultiplier, this.message.getSetting());
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int newCount) {
        this.messageCount = newCount;
    }

    // Do NOT call this to edit the container. Use NotificationManager methods instead.
    void editMessage(String newMessage) {
        this.message.setText(newMessage);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        MessageContainer that = (MessageContainer) other;
        return messageCount == that.messageCount && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, messageCount);
    }
}
