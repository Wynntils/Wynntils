/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;

public class MessageContainer {
    protected TextRenderTask message;
    private int iterations;

    public MessageContainer(String message) {
        this.message = new TextRenderTask(message, TextRenderSetting.DEFAULT);
        this.iterations = 0;
    }

    public MessageContainer(TextRenderTask message) {
        this.message = message;
        this.iterations = 0;
    }

    public TextRenderTask getRenderTask() {
        String sendableMessage = message.getText() + duplicateMessageBuilder(this);
        return new TextRenderTask(sendableMessage, this.message.getSetting());
    }

    public Integer getIterations() {
        return iterations;
    }

    public void editMessage(String newMessage, boolean incrementIterations) {
        this.message.setText(newMessage);
    }

    public void update(MessageContainer other) {
        this.message = other.message;
    }

    public void incrementIterations() {
        this.iterations++;
    }

    private String duplicateMessageBuilder(MessageContainer container) {
        if(this.iterations == 1 || this.iterations == 0) 
            return "";
        return " §7[x" + this.iterations + "]";
    }
}
