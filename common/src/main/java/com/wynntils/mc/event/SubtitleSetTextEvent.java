/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.ICancellableEvent;

public class SubtitleSetTextEvent extends BaseEvent implements ICancellableEvent {
    private final Component component;

    public SubtitleSetTextEvent(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}
