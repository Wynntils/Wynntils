/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.command;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.LinkedList;
import java.util.Queue;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandHandler extends Handler {
    private final Queue<String> commandQueue = new LinkedList<>();
    private int commandQueueTicks = 0;
    private static final int TICKS_PER_EXECUTE = 7;

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (!Models.WorldState.onWorld()) return;
        commandQueueTicks++;

        if (commandQueueTicks >= TICKS_PER_EXECUTE && !commandQueue.isEmpty()) {
            String command = commandQueue.poll();
            WynntilsMod.info("Executing queued command: " + command);
            McUtils.mc().getConnection().sendCommand(command);
            commandQueueTicks = 0;
        }
    }

    /**
     * Sends a command to the Wynncraft server. Commands will be sent as soon as possible, respecting the
     * server ratelimit. Queued commands may take up to {@link TICKS_PER_EXECUTE} each to execute.
     * If a queue is required, executions happen in the order they are queued.
     * All commands automatically log when they are executed.
     * @param command The command to queue. The leading '/' should not be included.
     */
    public void sendCommand(String command) {
        if (commandQueueTicks >= TICKS_PER_EXECUTE) {
            WynntilsMod.info("Executing immediate command: " + command);
            McUtils.mc().getConnection().sendCommand(command);
            commandQueueTicks = 0;
        } else {
            commandQueue.add(command);
        }
    }
}
