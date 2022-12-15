/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc;

import com.wynntils.core.managers.CoreManager;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// Use this manager to schedule runnables to run on next tick
public class MinecraftSchedulerManager extends CoreManager {
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public MinecraftSchedulerManager() {
        super(List.of());
    }

    @Override
    public void init() {}

    public void queueRunnable(Runnable runnable) {
        queue.add(runnable);
    }

    public void onTick() {
        while (!queue.isEmpty()) {
            queue.remove().run();
        }
    }
}
