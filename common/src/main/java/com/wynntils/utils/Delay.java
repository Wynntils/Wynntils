/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class Delay {
    private final Runnable function;
    private int delay;
    private boolean isRunning = true;
    private boolean onPause = false;

    private Delay(Runnable function, int delay) {
        this.function = function;
        this.delay = delay;

        WynntilsMod.registerEventListener(this);
    }

    public static Delay create(Runnable function, int delay) {
        return new Delay(function, delay);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.End e) {
        if (!onPause && isRunning) {
            if (delay < 0) {
                start();
            }
            delay--;
        }
    }

    public boolean isRunning() {
        return isRunning && !onPause;
    }

    public boolean pause() {
        if (!onPause && isRunning) {
            onPause = true;
            return true; // success
        }

        return false;
    }

    public boolean resume() {
        if (onPause && isRunning) {
            onPause = false;
            return true; // success
        }

        return false;
    }

    public void start() {
        isRunning = false;
        function.run();
        end();
    }

    public void end() {
        isRunning = false;
        WynntilsMod.unregisterEventListener(this);
    }
}
