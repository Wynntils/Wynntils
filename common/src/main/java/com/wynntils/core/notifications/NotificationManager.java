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
import com.wynntils.utils.objects.TimedSet;
import com.wynntils.wynn.event.NotificationEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class NotificationManager {
    private static final TimedSet<MessageContainer> cachedMessageSet = new TimedSet<>(10, TimeUnit.SECONDS, true);

    // Clear cached messages on world change
    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        cachedMessageSet.clear();
    }

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
        String messageText = message.getText();

        for (MessageContainer cachedContainer : cachedMessageSet) {
            String checkableMessage = cachedContainer.getOriginalMessage();
            if (messageText.equals(checkableMessage)) {
                cachedContainer.incrementMessageCount();

                WynntilsMod.postEvent(new NotificationEvent.Edit(cachedContainer));
                sendToChatIfNeeded(cachedContainer);

                return cachedContainer;
            }
        }

        cachedMessageSet.put(msgContainer);

        WynntilsMod.postEvent(new NotificationEvent.Queue(msgContainer));
        sendToChatIfNeeded(msgContainer);

        return msgContainer;
    }

    public static void editMessage(MessageContainer msgContainer, String newMessage) {
        msgContainer.editMessage(newMessage);

        WynntilsMod.postEvent(new NotificationEvent.Edit(msgContainer));
        sendToChatIfNeeded(msgContainer);
    }

    private static void sendToChatIfNeeded(MessageContainer container) {
        // Overlay is not enabled, send in chat
        if (!GameNotificationOverlayFeature.INSTANCE.isEnabled()) {
            sendOrEditNotification(container);
        }
    }

    private static void sendOrEditNotification(MessageContainer msgContainer) {
        McUtils.mc()
                .gui
                .getChat()
                .addMessage(new TextComponent(msgContainer.getRenderTask().getText()), msgContainer.hashCode());
    }
}
