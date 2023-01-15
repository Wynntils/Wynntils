/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class SubtitleSetTextEvent extends WynntilsEvent {
    private final Component component;

    public SubtitleSetTextEvent(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}
