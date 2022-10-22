/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import java.util.regex.Pattern;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

public final class WynnPlayerUtils {
    private static final Pattern PLAYER_GHOST_REGEX = Pattern.compile("_\\d+");

    public static boolean isPlayerGhost(Player player) {
        Team team = player.getTeam();

        if (team == null) return false;

        return PLAYER_GHOST_REGEX.matcher(team.getName()).find();
    }

    public static boolean isNpc(Player player) {
        String scoreboardName = player.getScoreboardName();
        return isNpc(scoreboardName);
    }

    public static boolean isNpc(String name) {
        return name.contains("\u0001") || name.contains("§");
    }

    // Returns true if the player is on the same server and is not a npc
    public static boolean isLocalPlayer(Player player) {
        return !isNpc(player) && !isPlayerGhost(player);
    }
}
