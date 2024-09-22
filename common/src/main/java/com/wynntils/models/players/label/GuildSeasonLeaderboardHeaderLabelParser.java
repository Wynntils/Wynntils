/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class GuildSeasonLeaderboardHeaderLabelParser implements LabelParser<GuildSeasonLeaderboardHeaderLabelInfo> {
    private static final Pattern HEADER_PATTERN = Pattern.compile("§d§lSeason (\\d+) Leaderboard");

    @Override
    public GuildSeasonLeaderboardHeaderLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = HEADER_PATTERN.matcher(label.getString());
        if (!matcher.matches()) {
            return null;
        }

        return new GuildSeasonLeaderboardHeaderLabelInfo(label, location, entity, Integer.parseInt(matcher.group(1)));
    }
}
