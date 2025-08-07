/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.bossbars;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombType;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoBar extends TrackedBar {
    // Test in InfoBar_TERRITORY_INFO_PATTERN
    private static final Pattern TERRITORY_INFO_PATTERN =
            Pattern.compile("§[abc](?<territory>[a-zA-Z\\s]+)§[234] (?<tag>\uE060\uDAFF\uDFFF.*\uDB00\uDC02)");

    // Test in InfoBar_GUILD_INFO_PATTERN
    private static final Pattern GUILD_INFO_PATTERN =
            Pattern.compile("§7Lv\\. (?<level>\\d+)§f - §b(?<guild>[a-zA-Z\\s]+)§f - §7(?<xp>\\d+)% XP");

    // Test in InfoBar_BOMB_INFO_PATTERN
    private static final Pattern BOMB_INFO_PATTERN = Pattern.compile(
            "§#a0c84bff(?:Double )?(?<bomb>.+) from §#ffd750ff(?<user>.+)§#a0c84bff §7\\[§f(?<length>\\d+)(?<unit>m|s)§7\\]");

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

            float length = Integer.parseInt(matcher.group("length"));
            if (matcher.group("unit").equals("m")) {
                length += BOMB_TIMER_OFFSET;
            } else {
                length /= 60f;
            }

            Models.Bomb.addBombInfoFromInfoBar(new BombInfo(
                    matcher.group("user"),
                    bombType,
                    Models.WorldState.getCurrentWorldName(),
                    System.currentTimeMillis(),
                    length));
        } else if (matcher.pattern().equals(GUILD_INFO_PATTERN)) {
            Models.Guild.setGuildLevel(Integer.parseInt(matcher.group("level")));
            Models.Guild.setGuildLevelProgress(new CappedValue(Integer.parseInt(matcher.group("xp")), 100));
        }
    }
}
