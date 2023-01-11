/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc;

import com.wynntils.core.components.Manager;
import com.wynntils.mc.event.TickEvent;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// Use this manager to schedule runnables to run on next tick
public final class MinecraftSchedulerManager extends Manager {
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public MinecraftSchedulerManager() {
        super(List.of());
    }

    public void queueRunnable(Runnable runnable) {
        queue.add(runnable);
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        while (!queue.isEmpty()) {
            queue.remove().run();
        }
    }
}
