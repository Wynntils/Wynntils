/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npc.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Pattern;

public class NpcLabelParser implements LabelParser {
    private static final Pattern NPC_LABEL_PATTERN = Pattern.compile("^§d(.+)$");

    // Special cases
    private static final Pattern TRADE_MARKET_LABEL_PATTERN = Pattern.compile("^§cTrade Market$");
    private static final Pattern HOUSING_LABEL_PATTERN = Pattern.compile("^§fClick §7to go to your housing plot$");

    public LabelInfo getInfo(StyledText label, Location location) {
        if (label.matches(NPC_LABEL_PATTERN)) {
            return new NpcLabelInfo(label, location.offset(0, -1, 0));
        } else if (label.matches(TRADE_MARKET_LABEL_PATTERN)) {
            return new NpcLabelInfo(label, location.offset(0, -1, 0));
        } else if (label.matches(HOUSING_LABEL_PATTERN)) {
            return new NpcLabelInfo(label, "Housing", location);
        }

        return null;
    }
}
