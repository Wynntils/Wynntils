/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.label;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class DamageLabelParser implements LabelParser<DamageLabelInfo> {
    private static final String FMT_NOISE = "(?:§\\{[^}]*}|§.)*";
    private static final String TYPE_COLOR = "§([245bcef])";
    private static final String NUMBER = "(\\d+(?:\\.\\d+)?)";
    private static final String SUFFIX = "([kKmMbB]?)";
    private static final String SEP_OR_END = "(?:󐀊|$)";

    private static final String DAMAGE_LABEL_PART =
            FMT_NOISE + TYPE_COLOR + FMT_NOISE + NUMBER + SUFFIX + FMT_NOISE + SEP_OR_END;

    private static final Pattern DAMAGE_LABEL_PART_PATTERN = Pattern.compile(DAMAGE_LABEL_PART);
    // Test in DamageLabelParser_DAMAGE_LABEL_PATTERN
    private static final Pattern DAMAGE_LABEL_PATTERN = Pattern.compile("(?:" + DAMAGE_LABEL_PART + ")+");

    private static final Map<Character, DamageType> TYPE_BY_COLOR = Map.of(
            '2', DamageType.EARTH,
            '5', DamageType.WATER,
            'b', DamageType.AIR,
            'c', DamageType.FIRE,
            'e', DamageType.THUNDER,
            'f', DamageType.NEUTRAL,
            '4', DamageType.NEUTRAL);

    @Override
    public DamageLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = label.getMatcher(DAMAGE_LABEL_PATTERN);
        if (!matcher.matches()) return null;

        Map<DamageType, Long> damages = new HashMap<>();

        Matcher partMatcher = label.getMatcher(DAMAGE_LABEL_PART_PATTERN);
        while (partMatcher.find()) {
            char color = Character.toLowerCase(partMatcher.group(1).charAt(0));
            String numberStr = partMatcher.group(2);
            String suffixStr = partMatcher.group(3);

            DamageType damageType = TYPE_BY_COLOR.get(color);
            if (damageType == null) {
                WynntilsMod.warn("Unknown damage type '" + color + "' in label: " + label.getString());
                continue;
            }

            long damage;
            try {
                damage = MathUtils.parseAbbreviatedNumber(numberStr, suffixStr);
            } catch (NumberFormatException e) {
                WynntilsMod.warn(
                        "Failed to parse damage amount: " + numberStr + suffixStr + " in label: " + label.getString());
                continue;
            }

            damages.put(damageType, damage);
        }

        if (damages.isEmpty()) {
            WynntilsMod.warn("No valid damage types found in label: " + label.getString());
            return null;
        }
        return new DamageLabelInfo(label, location, entity, damages);
    }
}
