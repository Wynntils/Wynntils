/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;

public class ActionBarMessageSetEvent extends Event {
    private Component message;

    public ActionBarMessageSetEvent(Component message) {
        this.message = message;
    }

    public void setMessage(Component message) {
        this.message = message;
    }

    public Component getMessage() {
        return message;
    }
}
