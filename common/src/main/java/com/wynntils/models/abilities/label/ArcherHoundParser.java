/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class ArcherHoundParser implements LabelParser<ArcherHoundInfo> {
    private static final Pattern HOUND_PATTERN = Pattern.compile("^(?<player>\\S+)('s|') Hound\\n(?<seconds>\\d+)s");

    @Override
    public ArcherHoundInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = HOUND_PATTERN.matcher(label.getStringWithoutFormatting());
        if (!matcher.matches()) return null;

        String playerName = matcher.group("player");
        int secondsLeft = Integer.parseInt(matcher.group("seconds"));

        return new ArcherHoundInfo(label, location, entity, secondsLeft, playerName);
    }
}
