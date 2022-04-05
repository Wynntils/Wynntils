/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils;

import java.util.regex.Pattern;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

public class WynnPlayerUtils {
    private static final Pattern PLAYER_GHOST_REGEX = Pattern.compile("_\\d+");

    public static boolean isPlayerGhost(Player player) {
        Team team = player.getTeam();

        if (team == null) return false;

        return PLAYER_GHOST_REGEX.matcher(team.getName()).find();
    }
}
