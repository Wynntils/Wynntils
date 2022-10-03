/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.core.WynntilsMod;
import com.wynntils.features.user.overlays.GameNotificationOverlayFeature;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.NotificationEvent;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public final class NotificationManager {
    public static MessageContainer queueMessage(String message) {
        return queueMessage(new TextRenderTask(message, TextRenderSetting.DEFAULT));
    }

    public static void queueMessage(Component message) {
        queueMessage(new TextRenderTask(ComponentUtils.getCoded(message), TextRenderSetting.DEFAULT));
    }

    public static MessageContainer queueMessage(TextRenderTask message) {
        if (!WynnUtils.onWorld()) return null;

        WynntilsMod.info("Message Queued: " + message);
        MessageContainer msgContainer = new MessageContainer(message);

        WynntilsMod.postEvent(new NotificationEvent.Queue(msgContainer));

        // Overlay is not enabled, send in chat
        if (!GameNotificationOverlayFeature.getInstance().isEnabled()) {
            sendOrEditNotification(msgContainer);
        }

        return msgContainer;
    }

    public static void editMessage(MessageContainer msgContainer, String newMessage) {
        msgContainer.editMessage(newMessage);

        WynntilsMod.postEvent(new NotificationEvent.Edit(msgContainer));

        // Overlay is not enabled, send in chat
        if (!GameNotificationOverlayFeature.getInstance().isEnabled()) {
            sendOrEditNotification(msgContainer);
        }
    }

    private static void sendOrEditNotification(MessageContainer msgContainer) {
        McUtils.mc()
                .gui
                .getChat()
                .addMessage(new TextComponent(msgContainer.getRenderTask().getText()), msgContainer.hashCode());
    }
}
