/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class GuildSeasonLeaderboardLabelParser implements LabelParser {
    // Test in GuildSeasonLeaderboardLabelParser_GUILD_SEASON_LEADERBOARD_PATTERN
    private static final Pattern GUILD_SEASON_LEADERBOARD_LABEL =
            Pattern.compile("^§.(?:§l)?(?<place>\\d+)(?:§7)? - §b(?<guild>.+)§d \\((?<score>[\\d\\s]+) SR\\)$");

    @Override
    public LabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = label.getMatcher(GUILD_SEASON_LEADERBOARD_LABEL);
        if (matcher.matches()) {
            int place = Integer.parseInt(matcher.group("place"));
            String guild = matcher.group("guild");
            long score = Long.parseLong(matcher.group("score").replace(" ", ""));

            return new GuildSeasonLeaderboardLabelInfo(label, location, entity, guild, place, score);
        }

        return null;
    }
}
