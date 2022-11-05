/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.managers.Model;
import com.wynntils.wynn.model.scoreboard.Segment;
import com.wynntils.wynn.model.scoreboard.guild.GuildAttackHandler;
import com.wynntils.wynn.model.scoreboard.guild.TerritoryAttackTimer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildAttackTimerModel extends Model {
    private static final Pattern GUILD_ATTACK_PATTERN = Pattern.compile("§b- (.+:.+) §3(.+)");
    public static final GuildAttackHandler SCOREBOARD_HANDLER = new GuildAttackHandler();

    private static List<TerritoryAttackTimer> attackTimers = List.of();

    public static void init() {}

    public static void processChanges(Segment segment) {
        List<TerritoryAttackTimer> newList = new ArrayList<>();

        for (String line : segment.getContent()) {
            Matcher matcher = GUILD_ATTACK_PATTERN.matcher(line);

            if (matcher.matches()) {
                newList.add(new TerritoryAttackTimer(matcher.group(2), matcher.group(1)));
            }
        }

        attackTimers = newList;
    }

    public static void resetTimers() {
        attackTimers = List.of();
    }

    public static List<TerritoryAttackTimer> getAttackTimers() {
        return attackTimers;
    }
}
