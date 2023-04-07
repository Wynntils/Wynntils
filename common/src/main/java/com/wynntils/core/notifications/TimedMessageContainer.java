/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.utils.render.TextRenderTask;

public class TimedMessageContainer {
    private MessageContainer messageContainer;
    private long endTime;

    public TimedMessageContainer(MessageContainer messageContainer, long messageDisplayLength) {
        this.messageContainer = messageContainer;
        this.endTime = messageDisplayLength + System.currentTimeMillis();
    }

    public void resetRemainingTime(long messageDisplayLength) {
        this.endTime = messageDisplayLength + System.currentTimeMillis();
    }

    public MessageContainer getMessageContainer() {
        return messageContainer;
    }

    public void setMessageContainer(MessageContainer messageContainer) {
        this.messageContainer = messageContainer;
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
