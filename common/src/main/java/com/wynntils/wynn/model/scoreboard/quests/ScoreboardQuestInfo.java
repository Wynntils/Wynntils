/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.scoreboard.quests;

import com.wynntils.mc.objects.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ScoreboardQuestInfo(String quest, String description) {
    private static final Pattern COORDINATE_PATTERN = Pattern.compile(".*\\[(-?\\d+), ?(-?\\d+), ?(-?\\d+)\\].*");

    public Location getLocation() {
        Matcher matcher = COORDINATE_PATTERN.matcher(this.description());
        if (!matcher.matches()) return null;

        return new Location(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));
    }
}
