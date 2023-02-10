/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.event;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;

public class EntityLabelChangedEvent extends Event {
    private final Component component;

    public EntityLabelChangedEvent(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}
