/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class WorldEventNameParser implements LabelParser<WorldEventNameInfo> {
    private static final Pattern WORLD_EVENT_NAME_PATTERN = Pattern.compile("^§#ebf7ffff(.*)$");

    public WorldEventNameInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = WORLD_EVENT_NAME_PATTERN.matcher(label.getString());
        if (!matcher.matches()) return null;

        return new WorldEventNameInfo(label, location, entity, matcher.group(1));
    }
}
