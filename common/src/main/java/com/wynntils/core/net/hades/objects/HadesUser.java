/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.hades.objects;

import com.wynntils.hades.protocol.packets.server.HSPacketUpdateMutual;
import com.wynntils.models.map.PoiLocation;
import com.wynntils.utils.CommonColors;
import com.wynntils.utils.CustomColor;
import java.util.UUID;

public class HadesUser {
    private final UUID uuid;
    private final String name;

    private boolean isPartyMember;
    private boolean isMutualFriend;
    private boolean isGuildMember;
    private float x, y, z;
    private PoiLocation poiLocation;
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

    public boolean isPartyMember() {
        return isPartyMember;
    }

    public boolean isMutualFriend() {
        return isMutualFriend;
    }

    public boolean isGuildMember() {
        return isGuildMember;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public PoiLocation getMapLocation() {
        return poiLocation;
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
        this.poiLocation = new PoiLocation((int) x, (int) y, (int) z);

        this.health = packet.getHealth();
        this.maxHealth = packet.getMaxHealth();

        this.mana = packet.getMana();
        this.maxMana = packet.getMaxMana();

        this.isPartyMember = packet.isPartyMember();
        this.isMutualFriend = packet.isMutualFriend();
        this.isGuildMember = packet.isGuildMember();
    }

    public CustomColor getRelationColor() {
        if (isPartyMember) return CommonColors.YELLOW;
        if (isMutualFriend) return CommonColors.GREEN;
        if (isGuildMember) return CommonColors.LIGHT_BLUE;

        return CustomColor.NONE;
    }
}
