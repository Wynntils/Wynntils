/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.internal;

import com.wynntils.core.features.InternalFeature;
import com.wynntils.features.user.overlays.GameNotificationOverlayFeature;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.event.NotificationEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NotificationsFeature extends InternalFeature {

    // Handles both edit and queue
    @SubscribeEvent
    public void onGameNotification(NotificationEvent event) {
        if (GameNotificationOverlayFeature.getInstance().isEnabled()) return;

        McUtils.mc()
                .gui
                .getChat()
                .addMessage(
                        new TextComponent(
                                event.getMessageContainer().getRenderTask().getText()),
                        event.getMessageContainer().hashCode());
    }
}
