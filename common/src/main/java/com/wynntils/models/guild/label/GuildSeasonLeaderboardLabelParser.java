/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.guild.label;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.guild.type.GuildLeaderboardInfo;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class GuildSeasonLeaderboardLabelParser implements LabelParser {
    private static final Pattern GUILD_SEASON_LEADERBOARD_LABEL = Pattern.compile(
            "§d§lSeason (?<season>\\d+) Leaderboard\n§7Season (?:ends in §b§l(?<remaining>\\d+) (?<timeunit>(month|week|day|hour|minute|second)s?)|ended at §b§l(?<month>\\d{2})/(?<day>\\d{2})/(?<year>\\d{4}))\n\n(?<guilds>.*?)\n§(?<firstpage>7|a)§l«§e ⬟ §(?<lastpage>7|a)§l»\n§eClick for Options",
            Pattern.DOTALL);
    private static final Pattern GUILD_LEADERBOARD_POSITION =
            Pattern.compile("^§.(?:§l)?(?<place>\\d+)(?:§7)? - §b(?<guild>.+)§d \\((\\d{1,3}(?:,\\d{3})*) SR\\)$");
    private static final String PAGE_END_CHARACTER = "7";

    @Override
    public LabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = label.getMatcher(GUILD_SEASON_LEADERBOARD_LABEL);
        if (matcher.matches()) {
            int season = Integer.parseInt(matcher.group("season"));
            String[] guildPositions = matcher.group("guilds").split("\n");
            boolean currentSeason = matcher.group("remaining") != null;
            List<Integer> endingDate = new ArrayList<>();
            String timeUnit = "";

            if (currentSeason) {
                endingDate.add(Integer.parseInt(matcher.group("remaining")));
                timeUnit = matcher.group("timeunit");
            } else {
                endingDate.add(Integer.parseInt(matcher.group("month")));
                endingDate.add(Integer.parseInt(matcher.group("day")));
                endingDate.add(Integer.parseInt(matcher.group("year")));
            }

            List<GuildLeaderboardInfo> guildLeaderboardInfo = new ArrayList<>();

            for (String guildStanding : guildPositions) {
                Matcher guildMatcher = GUILD_LEADERBOARD_POSITION.matcher(guildStanding);

                if (guildMatcher.matches()) {
                    int position = Integer.parseInt(guildMatcher.group(1));
                    String name = guildMatcher.group(2);
                    long rating = Long.parseLong(guildMatcher.group(3).replaceAll(",", ""));

                    guildLeaderboardInfo.add(new GuildLeaderboardInfo(position, name, rating));
                } else {
                    WynntilsMod.warn("Guild standing did not match: " + guildStanding);
                }
            }

            boolean firstPage = matcher.group("firstpage").equals(PAGE_END_CHARACTER);
            boolean lastPage = matcher.group("lastpage").equals(PAGE_END_CHARACTER);

            return new GuildSeasonLeaderboardLabelInfo(
                    label,
                    location,
                    entity,
                    season,
                    currentSeason,
                    timeUnit,
                    endingDate,
                    guildLeaderboardInfo,
                    firstPage,
                    lastPage);
        }

        return null;
    }
}
