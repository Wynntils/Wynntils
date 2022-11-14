/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.mc.utils.ComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;

// Fired when a message is sent to the local chat.
public class LocalMessageEvent extends Event {
    private final Component component;
    private final String codedMessage;

    public LocalMessageEvent(Component component) {
        this.component = component;
        this.codedMessage = ComponentUtils.getCoded(component);
    }

    public Component getComponent() {
        return component;
    }

    public String getCodedMessage() {
        return codedMessage;
    }
}
