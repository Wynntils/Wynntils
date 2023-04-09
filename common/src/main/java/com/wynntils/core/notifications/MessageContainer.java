/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.core.text.CodedString;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import java.util.Objects;

public class MessageContainer {
    private CodedString message;
    private TextRenderTask renderTask;
    private int messageCount;

    public MessageContainer(String message) {
        this(new TextRenderTask(message, TextRenderSetting.DEFAULT));
    }

    public MessageContainer(TextRenderTask message) {
        this.message = message.getText();
        this.renderTask = message;
        this.messageCount = 1;
    }

    public CodedString getMessage() {
        return message;
    }

    public TextRenderTask getRenderTask() {
        return renderTask;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int newCount) {
        this.messageCount = newCount;

        updateRenderTask();
    }

    // Do NOT call this to edit the container. Use Managers.Notification methods instead.
    void editMessage(CodedString newMessage) {
        this.message = newMessage;

        updateRenderTask();
    }

    private void updateRenderTask() {
        if (this.messageCount == 1) {
            this.renderTask = new TextRenderTask(this.message, TextRenderSetting.DEFAULT);
        } else {
            CodedString messageMultiplier = CodedString.fromString(" §7[x" + this.messageCount + "]");
            this.renderTask = new TextRenderTask(this.message.append(messageMultiplier), this.renderTask.getSetting());
        }
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
