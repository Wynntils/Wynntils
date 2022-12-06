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
    private static final TimedSet<Pair<TextRenderTask, MessageContainer>> cachedMessageSet = new TimedSet<>(5, TimeUnit.SECONDS, true);

    public static MessageContainer queueMessage(String message) {
        return queueMessage(new TextRenderTask(message, TextRenderSetting.DEFAULT));
    }

    public static void queueMessage(Component message) {
        queueMessage(new TextRenderTask(ComponentUtils.getCoded(message), TextRenderSetting.DEFAULT));
    }

    // Clear cached messages on world change
    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        cachedMessageSet.clear();
    }

    public static MessageContainer queueMessage(TextRenderTask message) {
        if (!WynnUtils.onWorld()) return null;

        WynntilsMod.info("Message Queued: " + message);
        MessageContainer msgContainer = new MessageContainer(message);
        Integer messageHash = message.getText().hashCode();

        for(Pair<TextRenderTask, MessageContainer> cachedMessagePair : cachedMessageSet) {
            Integer checkableHash = cachedMessagePair.a().hashCode();
            if (messageHash.equals(checkableHash)) {
                cachedMessagePair.b().editMessage(message.toString(), true);
                return cachedMessagePair.b();
            }
            else {
            cachedMessageSet.put(new Pair<>(message, msgContainer));
            continue; }
        }

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
