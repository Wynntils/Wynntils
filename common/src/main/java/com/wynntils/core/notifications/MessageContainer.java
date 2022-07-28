/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;

public class MessageContainer {
    TextRenderTask message;
    long endTime;

    public MessageContainer(String message, long messageTimeLimit) {
        this.message = new TextRenderTask(message, TextRenderSetting.DEFAULT);
        this.endTime = System.currentTimeMillis() + messageTimeLimit;
    }

    public MessageContainer(TextRenderTask message, long messageTimeLimit) {
        this.message = message;
        this.endTime = System.currentTimeMillis() + messageTimeLimit;
    }

    public long getRemainingTime() {
        return endTime - System.currentTimeMillis();
    }

    public long getEndTime() {
        return endTime;
    }

    public TextRenderTask getRenderTask() {
        return message;
    }

    public MessageContainer editMessage(String newMessage) {
        this.message.setText(newMessage);
        return this;
    }

    public MessageContainer resetRemainingTime(long messageTimeLimit) {
        this.endTime = System.currentTimeMillis() + messageTimeLimit;
        return this;
    }

    public void update(MessageContainer other) {
        this.message = other.message;
        this.endTime = other.endTime;
    }
}
