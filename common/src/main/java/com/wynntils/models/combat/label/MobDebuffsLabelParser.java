/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.world.entity.Entity;

public class MobDebuffsLabelParser implements LabelParser<MobDebuffsLabelInfo> {
    private static final String DEBUFF_SYMBOLS = Arrays.stream(DebuffType.values())
            .map(d -> String.valueOf(d.symbol()))
            .collect(Collectors.joining());
    private static final Pattern DEBUFF_PATTERN = Pattern.compile("§(?:#?[a-z0-9]{1,8})(?<symbol>[" + DEBUFF_SYMBOLS
            + "])(?:\\s*§7(?<value>\\d+(?:\\.\\d+)?)(?<unit>%|k|m|b)?)?");

    @Override
    public MobDebuffsLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = label.getMatcher(DEBUFF_PATTERN);

        if (!matcher.find()) return null;

        Map<DebuffType, Integer> debuffs = new EnumMap<>(DebuffType.class);

        do {
            char symbol = matcher.group("symbol").charAt(0);
            int value = 1;

            // Some debuffs don't have a value and are just active
            String rawValue = matcher.group("value");
            if (rawValue != null) {
                double parsed = Double.parseDouble(rawValue);
                String unit = matcher.group("unit");

                // Parse the debuffs with higher values
                if (unit != null) {
                    switch (unit) {
                        case "k" -> parsed *= 1_000;
                        case "m" -> parsed *= 1_000_000;
                        case "b" -> parsed *= 1_000_000_000;
                    }
                }

                value = (int) Math.round(parsed);
            }

            DebuffType type = DebuffType.fromSymbol(symbol);
            if (type != null) {
                debuffs.put(type, value);
            }
        } while (matcher.find());

        return new MobDebuffsLabelInfo(label, location, entity, debuffs);
    }
}
