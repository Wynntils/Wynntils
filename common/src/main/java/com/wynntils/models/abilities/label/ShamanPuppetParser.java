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

public class ShamanPuppetParser implements LabelParser<ShamanPuppetInfo> {
    private static Pattern PUPPET_PATTERN = Pattern.compile("^(?<player>\\S+)('s|') Puppet\\n\uE01F (?<seconds>\\d+)s");

    @Override
    public ShamanPuppetInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = PUPPET_PATTERN.matcher(label.getStringWithoutFormatting());
        if (!matcher.matches()) return null;

        int secondsLeft = Integer.parseInt(matcher.group("seconds"));
        String playerName = matcher.group("player");

        return new ShamanPuppetInfo(label, location, entity, secondsLeft, playerName);
    }
}
