/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hades.type;

import com.wynntils.hades.protocol.enums.PlayerPingType;
import com.wynntils.hades.protocol.packets.server.HSPacketPlayerPing;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.core.Direction;

public class PlayerPingData {
    private final String username;
    private final Location location;
    private final Direction direction;
    private final PlayerPingType pingType;
    private final String pingTarget;

    private PlayerPingData(
            String username, Location location, Direction direction, PlayerPingType pingType, String pingTarget) {
        this.username = username;
        this.location = location;
        this.direction = direction;
        this.pingType = pingType;
        this.pingTarget = pingTarget;
    }

    public static PlayerPingData fromPacket(HSPacketPlayerPing packet) {
        return new PlayerPingData(
                packet.getUsername(),
                Location.containing(packet.getX(), packet.getY(), packet.getZ()),
                Direction.valueOf(packet.getDirection().name()),
                packet.getPingType(),
                packet.getPingTarget());
    }

    public String getUsername() {
        return username;
    }

    public Location getLocation() {
        return location;
    }

    public Direction getDirection() {
        return direction;
    }

    public PlayerPingType getPingType() {
        return pingType;
    }

    public String getPingTarget() {
        return pingTarget;
    }
}
