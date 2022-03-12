/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Delay {

    private final Runnable function;
    private int delay;

    public Delay(Runnable function, int delay) {
        this.function = function;
        this.delay = delay;

        WynntilsMod.getEventBus().register(this);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent e) {
        if (e.getTickPhase() == ClientTickEvent.Phase.END) {
            if (--delay < 0) {
                start();
            }
        }
    }

    public void start() {
        function.run();
        WynntilsMod.getEventBus().unregister(this);
    }
}
