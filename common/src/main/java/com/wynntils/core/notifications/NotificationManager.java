/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wc.event.NotificationEvent;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.network.chat.Component;

public class NotificationManager {

    public static MessageContainer queueMessage(String message) {
        return queueMessage(new TextRenderTask(message, TextRenderSetting.DEFAULT));
    }

    public static MessageContainer queueMessage(Component message) {
        return queueMessage(new TextRenderTask(ComponentUtils.getCoded(message), TextRenderSetting.DEFAULT));
    }

    public static MessageContainer queueMessage(TextRenderTask message) {
        if (!WynnUtils.onWorld()) return null;

        WynntilsMod.info("Message Queued: " + message);
        MessageContainer msgContainer = new MessageContainer(message);

        WynntilsMod.getEventBus().post(new NotificationEvent.Queue(msgContainer));

        return msgContainer;
    }

    public static void editMessage(MessageContainer msgContainer, String newMessage) {
        msgContainer.editMessage(newMessage);

        WynntilsMod.getEventBus().post(new NotificationEvent.Edit(msgContainer));
    }
}
