/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class LerpingBossEventRenderEvent extends Event {
    private final LerpingBossEvent bossEvent;

    public LerpingBossEventRenderEvent(LerpingBossEvent bossEvent) {
        this.bossEvent = bossEvent;
    }

    public LerpingBossEvent getBossEvent() {
        return bossEvent;
    }
}
