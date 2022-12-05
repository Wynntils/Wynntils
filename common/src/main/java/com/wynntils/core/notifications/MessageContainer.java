/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;

public class MessageContainer {
    protected TextRenderTask message;
    private Integer iterations;

    public MessageContainer(String message) {
        this.message = new TextRenderTask(message, TextRenderSetting.DEFAULT);
        this.iterations = 1;
    }

    public MessageContainer(TextRenderTask message) {
        this.message = message;
        this.iterations = 1;
    }

    public TextRenderTask getRenderTask() {
        return message;
    }

    public Integer getIterations() {
        return iterations;
    }

    public void editMessage(String newMessage, Boolean incrementIterations) {
        String sendableMessage;
        if(incrementIterations) {
            this.iterations++;
            sendableMessage = newMessage + " [x" + this.iterations + "]";
        }
        else {
            sendableMessage = newMessage;
        }

        this.message.setText(sendableMessage);
    }

    public void update(MessageContainer other) {
        this.message = other.message;
    }
}
