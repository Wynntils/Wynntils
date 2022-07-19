/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.objects;

import com.wynntils.features.user.GameUpdateOverlayFeature;
import net.minecraft.network.chat.Component;

public class MessageContainer {
    Component message;
    long endTime;

    public MessageContainer(Component message) {
        this.message = message;
        this.endTime = System.currentTimeMillis()
                + (long) (GameUpdateOverlayFeature.getInstance().GameUpdateOverlay.messageTimeLimit * 1000);
    }

    public long getRemainingTime() {
        return endTime - System.currentTimeMillis();
    }

    public Component getMessage() {
        return message;
    }

    public void editMessage(Component newMessage) {
        this.message = newMessage;
    }

    public void resetRemainingTime() {
        this.endTime = System.currentTimeMillis()
                + (long) (GameUpdateOverlayFeature.getInstance().GameUpdateOverlay.messageTimeLimit * 1000);
    }
}
