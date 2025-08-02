/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.label;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.mc.type.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class DamageLabelParser implements LabelParser<DamageLabelInfo> {
    private static final String DAMAGE_LABEL_PART =
            "§[245bcef](?:§l)?-(\\d+) (?:§r§[245bcef](?:§l)?)?([❤\uE003\uE001\uE004\uE002\uE000☠]) ";

    private static final Pattern DAMAGE_LABEL_PART_PATTERN = Pattern.compile(DAMAGE_LABEL_PART);
    // Test in DamageLabelParser_DAMAGE_LABEL_PATTERN
    private static final Pattern DAMAGE_LABEL_PATTERN = Pattern.compile("(?:" + DAMAGE_LABEL_PART + ")+");

    @Override
    public DamageLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = label.getMatcher(DAMAGE_LABEL_PATTERN);
        if (!matcher.matches()) return null;

        Map<DamageType, Long> damages = new HashMap<>();

        Matcher partMatcher = label.getMatcher(DAMAGE_LABEL_PART_PATTERN);
        while (partMatcher.find()) {
            String damageStr = partMatcher.group(1);
            String damageTypeStr = partMatcher.group(2);

            long damage = Long.parseLong(damageStr);
            DamageType damageType = DamageType.fromSymbol(damageTypeStr);
            if (damageType == null) {
                WynntilsMod.warn("Unknown damage type: " + damageTypeStr);
                continue;
            }

            damages.put(damageType, damage);
        }

        if (damages.isEmpty()) {
            WynntilsMod.warn("No valid damage types found in label: " + label);
            return null;
        }

        return new DamageLabelInfo(label, location, entity, damages);
    }
}
