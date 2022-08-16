/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.mc.render.TextRenderTask;

public class TimedMessageContainer {
    private MessageContainer messageContainer;
    private long endTime;

    public TimedMessageContainer(MessageContainer messageContainer, long messageDisplayLength) {
        this.messageContainer = messageContainer;
        this.endTime = messageDisplayLength + System.currentTimeMillis();
    }

    public TimedMessageContainer resetRemainingTime(long messageDisplayLength) {
        this.endTime = messageDisplayLength + System.currentTimeMillis();
        return this;
    }

    public void update(TimedMessageContainer other, long messageDisplayLength) {
        this.messageContainer = other.messageContainer;
        resetRemainingTime(messageDisplayLength);
    }

    public void update(MessageContainer other, long messageDisplayLength) {
        this.messageContainer.update(other);
        resetRemainingTime(messageDisplayLength);
    }

    public TimedMessageContainer editMessage(String newMessage, long messageDisplayLength) {
        this.messageContainer.message.setText(newMessage);
        resetRemainingTime(messageDisplayLength);
        return this;
    }

    public MessageContainer getMessageContainer() {
        return messageContainer;
    }

    public long getRemainingTime() {
        return endTime - System.currentTimeMillis();
    }

    public TextRenderTask getRenderTask() {
        return this.messageContainer.getRenderTask();
    }

    public long getEndTime() {
        return this.endTime;
    }
}
