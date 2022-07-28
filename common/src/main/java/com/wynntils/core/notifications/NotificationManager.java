/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.wc.event.NotificationEvent;
import com.wynntils.wc.utils.WynnUtils;

public class NotificationManager {

    private static final long DEFAULT_MESSAGE_TIME_LIMIT = 10000;

    public static MessageContainer queueMessage(String message) {
        return queueMessage(new TextRenderTask(message, TextRenderSetting.DEFAULT));
    }

    public static MessageContainer queueMessage(TextRenderTask message) {
        if (!WynnUtils.onWorld()) return null;

        WynntilsMod.info("Message Queued: " + message);
        MessageContainer msgContainer = new MessageContainer(message, DEFAULT_MESSAGE_TIME_LIMIT);

        WynntilsMod.getEventBus().post(new NotificationEvent.Queue(msgContainer));

        return msgContainer;
    }

    public static void editMessage(MessageContainer msgContainer, String newMessage) {
        msgContainer.editMessage(newMessage);
        msgContainer.resetRemainingTime(DEFAULT_MESSAGE_TIME_LIMIT);

        WynntilsMod.getEventBus().post(new NotificationEvent.Edit(msgContainer));
    }
}
