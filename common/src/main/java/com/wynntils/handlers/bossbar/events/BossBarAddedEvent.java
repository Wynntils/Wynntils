/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.bossbar.events;

import com.wynntils.wynn.model.bossbar.BarType;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class BossBarAddedEvent extends Event {
    private final BarType type;

    public BossBarAddedEvent(BarType type) {
        this.type = type;
    }

    public BarType getType() {
        return type;
    }
}
