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
import java.util.concurrent.TimeUnit;
import com.wynntils.utils.objects.TimedSet;
import com.wynntils.utils.Pair;
import com.wynntils.wynn.event.WorldStateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class NotificationManager {
    private static final TimedSet<Pair<String, MessageContainer>> cachedMessageSet = new TimedSet<>(5, TimeUnit.SECONDS, true);

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

        for(Pair<String, MessageContainer> cachedMessagePair : cachedMessageSet) {
            Integer messageTextHash = messageText.hashCode();
            Integer iteratedMessageHash = cachedMessagePair.a().hashCode();
            if (messageTextHash.equals(iteratedMessageHash)) {
                WynntilsMod.info("Matched Message: " + message + " to existing message. Updating existing message.");
                editMessage(cachedMessagePair.b(), messageText, true);
                return cachedMessagePair.b();
            }
            else { continue; }
        }

        cachedMessageSet.put(new Pair<>(messageText, msgContainer));

        WynntilsMod.postEvent(new NotificationEvent.Queue(msgContainer));

        // Overlay is not enabled, send in chat
        if (!GameNotificationOverlayFeature.INSTANCE.isEnabled()) {
            sendOrEditNotification(msgContainer);
        }

        return msgContainer;
    }

    public static void editMessage(MessageContainer msgContainer, String newMessage, Boolean incrementIterations) {
        msgContainer.editMessage(newMessage, incrementIterations);

        WynntilsMod.postEvent(new NotificationEvent.Edit(msgContainer));

        // Overlay is not enabled, send in chat
        if (!GameNotificationOverlayFeature.INSTANCE.isEnabled()) {
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
