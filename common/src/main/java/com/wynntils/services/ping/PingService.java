/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.ping;

import com.wynntils.core.components.Service;
import com.wynntils.mc.event.PongReceivedEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.Util;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.neoforged.bus.api.SubscribeEvent;

public class PingService extends Service {
    private static final int MS_PER_PING = 1000;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private int lastPing = 0;

    public PingService() {
        super(List.of());
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) {
            executor.scheduleAtFixedRate(this::sendPingPacket, 0, MS_PER_PING, TimeUnit.MILLISECONDS);
        } else {
            executor.shutdownNow();
            executor = Executors.newSingleThreadScheduledExecutor();
        }
    }

    @SubscribeEvent
    public void onPongReceived(PongReceivedEvent event) {
        lastPing = (int) (Util.getMillis() - event.getTime());
    }

    private void sendPingPacket() {
        McUtils.sendPacket(new ServerboundPingRequestPacket(Util.getMillis()));
    }

    public int getPing() {
        return lastPing;
    }
}
