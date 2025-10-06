/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.label;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.Time;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class WorldEventCountdownParser implements LabelParser<WorldEventCountdownInfo> {
    private static final Pattern COUNTDOWN_PATTERN =
            Pattern.compile("^§#aeb8bfffStarts in(?: (?<hour>\\d+)h)?(?: (?<minute>\\d+)m)?(?: (?<second>\\d+)s)?$");

    public WorldEventCountdownInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = COUNTDOWN_PATTERN.matcher(label.getString());
        if (!matcher.matches()) return null;

        Time startTime = Models.WorldEvent.parseWorldEventStartTime(
                matcher.group("hour"), matcher.group("minute"), matcher.group("second"));

        return new WorldEventCountdownInfo(label, location, entity, startTime);
    }
}
