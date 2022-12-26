/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import java.util.UUID;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public final class PlayerInfoUtils {
    public static ResourceLocation getSkin(UUID uuid) {
        ClientPacketListener connection = McUtils.mc().getConnection();

        if (connection == null) {
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        PlayerInfo playerInfo = connection.getPlayerInfo(uuid);
        if (playerInfo == null) {
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }

        return playerInfo.getSkinLocation();
    }
}
