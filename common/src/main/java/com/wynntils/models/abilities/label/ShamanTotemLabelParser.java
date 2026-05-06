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

public class ShamanTotemLabelParser implements LabelParser<ShamanTotemLabelInfo> {
    // Test in ShamanTotemLabelParser_SHAMAN_TOTEM_PATTERN
    private static final Pattern SHAMAN_TOTEM_PATTERN =
            Pattern.compile("§b(?<playerName>.+)'(?:s)? §7Totem\n ?(?:§c\\+(?<regenPerSecond>\\d+)❤§7/s )?"
                    + "(?:§5\uE011 §7(?<poisonAmount>.+?) )?(?:§e\uE013 §7(?<invigorateTime>\\d+)s )?"
                    + "§d\uE01F §7(?<timeLeft>\\d+)s");

    @Override
    public ShamanTotemLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = label.getMatcher(SHAMAN_TOTEM_PATTERN);
        if (!matcher.matches()) return null;

        String playerName = matcher.group("playerName");
        int regenPerSecond = -1;
        if (matcher.group("regenPerSecond") != null) {
            regenPerSecond = Integer.parseInt(matcher.group("regenPerSecond"));
        }

        String poisonAmount = "";
        if (matcher.group("poisonAmount") != null) {
            poisonAmount = matcher.group("poisonAmount");
        }

        int invigorateTime = -1;
        if (matcher.group("invigorateTime") != null) {
            invigorateTime = Integer.parseInt(matcher.group("invigorateTime"));
        }

        int timeLeft = Integer.parseInt(matcher.group("timeLeft"));

        return new ShamanTotemLabelInfo(
                label, location, entity, playerName, regenPerSecond, poisonAmount, invigorateTime, timeLeft);
    }
}
