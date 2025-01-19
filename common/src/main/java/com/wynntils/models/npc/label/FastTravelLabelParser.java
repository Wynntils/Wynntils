/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npc.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class FastTravelLabelParser implements LabelParser<FastTravelLabelInfo> {
    // Test in FastTravelLabelParser_FAST_TRAVEL_LABEL_PATTERN
    private static final Pattern FAST_TRAVEL_LABEL_PATTERN = Pattern.compile(
            "§#8193ffff\uE060\uDAFF\uDFFF\uE035\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE045\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE062\uDAFF\uDFBE§0\uE005\uE000\uE012\uE013 \uE013\uE011\uE000\uE015\uE004\uE00B\uDB00\uDC02§#8193ffff\n§#f9e79eff(?<name>.+)\n§0 \n§7\uE01C §oTo (?:the )?(?<destination>.+)§r§7 \uE01C.*",
            Pattern.DOTALL);

    public FastTravelLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher fastTravelMatcher = FAST_TRAVEL_LABEL_PATTERN.matcher(label.getString());
        if (fastTravelMatcher.matches()) {
            String name = fastTravelMatcher.group("name");

            String destination = fastTravelMatcher.group("destination");

            return new FastTravelLabelInfo(label, name, location, entity, destination);
        }

        return null;
    }
}
