/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.bossbars;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombType;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoBar extends TrackedBar {
    // §cDetlas§4 [AVO]
    private static final Pattern TERRITORY_INFO_PATTERN =
            Pattern.compile("§.(?<territory>.+)§. \\[(?<tag>[A-Za-z]{3,4})\\]");

    // §7Lv. 92§f - §bKingdom Foxes§f - §762% XP
    private static final Pattern GUILD_INFO_PATTERN =
            Pattern.compile("§7Lv. (?<level>\\d+)§f - §b(?<guild>.+)§f - §7(?<xp>\\d+)% XP");

    // §3Double Profession Speed from §bCorkian§7 [§f2§7 min]
    private static final Pattern BOMB_INFO_PATTERN =
            Pattern.compile("§3(?:Double )?(?<bomb>.+) from §b(?<user>.+)§7 \\[§f(?<length>\\d+)§7 min\\]");

    // Minute info is rounded, half a minute offset is a good compromise
    private static final float BOMB_TIMER_OFFSET = 0.5f;

    public InfoBar() {
        super(List.of(TERRITORY_INFO_PATTERN, GUILD_INFO_PATTERN, BOMB_INFO_PATTERN));
    }

    @Override
    public void onUpdateName(Matcher matcher) {
        if (matcher.pattern().equals(BOMB_INFO_PATTERN)) {
            BombType bombType = BombType.fromString(matcher.group("bomb"));

            if (bombType == null) return;

            float length = Integer.parseInt(matcher.group("length")) + BOMB_TIMER_OFFSET;
            Models.Bomb.addBombInfo(
                    bombType,
                    new BombInfo(
                            matcher.group("user"),
                            bombType,
                            Models.WorldState.getCurrentWorldName(),
                            System.currentTimeMillis(),
                            length));
        }
    }
}
