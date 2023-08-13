/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.notifications.event.NotificationEvent;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.overlays.GameNotificationOverlayFeature;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.type.TimedSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class NotificationManager extends Manager {
    private static final TimedSet<MessageContainer> cachedMessageSet = new TimedSet<>(10, TimeUnit.SECONDS, true);

    public NotificationManager() {
        super(List.of());
    }

    // Clear cached messages on world change
    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        cachedMessageSet.clear();
    }

    public MessageContainer queueMessage(StyledText styledText) {
        return queueMessage(new TextRenderTask(styledText, TextRenderSetting.DEFAULT));
    }

    public MessageContainer queueMessage(Component message) {
        return queueMessage(new TextRenderTask(StyledText.fromComponent(message), TextRenderSetting.DEFAULT));
    }

    public MessageContainer queueMessage(TextRenderTask message) {
        if (!Managers.Connection.onServer()) return null;

        WynntilsMod.info("Message Queued: " + message);
        MessageContainer msgContainer = new MessageContainer(message);
        StyledText messageText = message.getText();

        for (MessageContainer cachedContainer : cachedMessageSet) {
            StyledText checkableMessage = cachedContainer.getMessage();
            if (messageText.equals(checkableMessage)) {
                cachedContainer.setMessageCount(cachedContainer.getMessageCount() + 1);

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

    /**
     * Edits a message in the queue.
     * If the edited MessageContainer has repeated messages,
     * the old message container's message count is decreased by one,
     * and a new message container is created with the new message.
     * @param msgContainer The message container to edit
     * @param newMessage The new message
     * @return The message container that was edited. This may be the new message container.
     */
    public MessageContainer editMessage(MessageContainer msgContainer, StyledText newMessage) {
        WynntilsMod.info("Message Edited: " + msgContainer.getRenderTask() + " -> " + newMessage.getString());

        // If we have multiple repeated messages, we want to only edit the last one.
        if (msgContainer.getMessageCount() > 1) {
            // Decrease the message count of the old message
            msgContainer.setMessageCount(msgContainer.getMessageCount() - 1);

            // Let the mod know that the message was edited
            WynntilsMod.postEvent(new NotificationEvent.Edit(msgContainer));
            sendToChatIfNeeded(msgContainer);

            // Then, queue the new message
            return queueMessage(newMessage);
        } else {
            msgContainer.editMessage(newMessage);

            WynntilsMod.postEvent(new NotificationEvent.Edit(msgContainer));
            sendToChatIfNeeded(msgContainer);

            return msgContainer;
        }
    }

    private void sendToChatIfNeeded(MessageContainer container) {
        // Overlay is not enabled, send in chat
        if (!Managers.Feature.getFeatureInstance(GameNotificationOverlayFeature.class)
                .isEnabled()) {
            sendOrEditNotification(container);
        }
    }

    private void sendOrEditNotification(MessageContainer msgContainer) {
        McUtils.mc()
                .gui
                .getChat()
                .addMessage(msgContainer.getRenderTask().getText().getComponent());
    }
}
