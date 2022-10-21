/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.sockets.objects;

import com.wynntils.hades.protocol.enums.RelationType;
import com.wynntils.hades.protocol.packets.server.HSPacketUpdateMutual;
import java.util.UUID;

public class HadesUser {
    private final UUID uuid;
    private final String name;

    private RelationType relationType;

    private double x, y, z;
    private int health, maxHealth;
    private int mana, maxMana;

    public HadesUser(HSPacketUpdateMutual packet) {
        uuid = packet.getUser();
        name = packet.getName();

        this.updateFromPacket(packet);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public RelationType getRelationType() {
        return relationType;
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

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getMana() {
        return mana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public void updateFromPacket(HSPacketUpdateMutual packet) {
        this.x = packet.getX();
        this.y = packet.getY();
        this.z = packet.getZ();

        this.health = packet.getHealth();
        this.maxHealth = packet.getMaxHealth();

        this.mana = packet.getMana();
        this.maxMana = packet.getMaxMana();

        this.relationType = packet.getRelationType();
    }
}
