/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.mount.type.MountEnclosure;
import com.wynntils.utils.mc.type.Location;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class MountEnclosureLabelParser implements LabelParser<MountEnclosureLabelInfo> {
    private static final Pattern MOUNT_ENCLOSURE_PATTERN =
            Pattern.compile("§#bc8f62ff(?<mountEnclosure>Ternaves Ranch|Bantisu Quarters|Aldwell Sanctuary)");

    @Override
    public MountEnclosureLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = label.getMatcher(MOUNT_ENCLOSURE_PATTERN);

        if (matcher.matches())
            return new MountEnclosureLabelInfo(
                    label,
                    location,
                    entity,
                    MountEnclosure.valueOf(matcher.group("mountEnclosure")
                            .toUpperCase(Locale.ROOT)
                            .replace(" ", "_")));
        return null;
    }
}