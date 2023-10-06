/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GatheringStationLabelParser implements LabelParser {
    private static final Pattern GATHERING_STATION_LABEL_PATTERN =
            Pattern.compile("^§f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] §6§l(.+)§f [ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]$");

    @Override
    public LabelInfo getInfo(StyledText label, Location location) {
        Matcher matcher = label.getMatcher(GATHERING_STATION_LABEL_PATTERN);
        if (matcher.matches()) {
            ProfessionType professionType = ProfessionType.fromString(matcher.group(1));
            if (professionType == null) return null;

            return new ProfessionCraftingStationLabelInfo(
                    label, matcher.group(1) + " Station", location.offset(0, -2, 0), professionType);
        }

        return null;
    }
}
