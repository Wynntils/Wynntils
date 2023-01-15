/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import java.util.UUID;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PlayerJoinedWorldEvent extends WynntilsEvent {
    private final int entityId;
    private final UUID playerId;
    private final double x;
    private final double y;
    private final double z;
    private final byte yRot;
    private final byte xRot;
    private final PlayerInfo playerInfo;

    public PlayerJoinedWorldEvent(
            int entityId, UUID playerId, double x, double y, double z, byte yRot, byte xRot, PlayerInfo playerInfo) {
        this.entityId = entityId;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yRot = yRot;
        this.xRot = xRot;
        this.playerInfo = playerInfo;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getEntityId() {
        return entityId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public byte getxRot() {
        return xRot;
    }

    public byte getyRot() {
        return yRot;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }
}
