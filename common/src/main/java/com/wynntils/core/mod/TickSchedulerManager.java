/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.mod;

import com.wynntils.core.components.Manager;
import com.wynntils.mc.event.TickAlwaysEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class TickSchedulerManager extends Manager {
    private final Map<ScheduledTask, Integer> tasks = new ConcurrentHashMap<>();

    public TickSchedulerManager() {
        super(List.of());
    }

    public ScheduledTask scheduleLater(Runnable runnable, int ticksDelay) {
        ScheduledTask task = new ScheduledTask(runnable);
        tasks.put(task, ticksDelay);
        return task;
    }

    public ScheduledTask scheduleNextTick(Runnable runnable) {
        return scheduleLater(runnable, 0);
    }

    public void cancel(ScheduledTask task) {
        tasks.remove(task);
    }

    // The priority is set to HIGHEST to ensure that the tasks are run
    // before any other tick event listeners could schedule new tasks
    // making it run in the same tick
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(TickAlwaysEvent e) {
        Iterator<Map.Entry<ScheduledTask, Integer>> it = tasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ScheduledTask, Integer> entry = it.next();
            int ticksLeft = entry.getValue();
            if (ticksLeft == 0) {
                // Run the task
                entry.getKey().task.run();
                // Remove it from the map
                it.remove();
            } else {
                entry.setValue(ticksLeft - 1);
            }
        }
    }

    public record ScheduledTask(Runnable task) {}
}
