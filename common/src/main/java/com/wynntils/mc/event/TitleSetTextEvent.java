/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.ICancellableEvent;

public class TitleSetTextEvent extends BaseEvent implements ICancellableEvent {
    private final Component component;

    public TitleSetTextEvent(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}
