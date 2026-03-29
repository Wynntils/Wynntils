/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MountAnnotator implements GameItemAnnotator {
    private static final Pattern MOUNT_PATTERN =
            Pattern.compile("([\\p{L}\\p{N}'\\- ]+)\\s+Whistle", Pattern.CASE_INSENSITIVE);
    private static final Map<MountStat, Pattern> STAT_PATTERNS = Map.of(
            MountStat.ACCELERATION, Pattern.compile("\\bAcceleration\\b.*?(\\d+)/(\\d+)\\b"),
            MountStat.ALTITUDE, Pattern.compile("\\bAltitude\\b.*?(\\d+)/(\\d+)\\b"),
            MountStat.ENERGY, Pattern.compile("\\bEnergy\\b.*?(\\d+)/(\\d+)\\b"),
            MountStat.HANDLING, Pattern.compile("\\bHandling\\b.*?(\\d+)/(\\d+)\\b"),
            MountStat.POWERUP, Pattern.compile("\\bPowerup\\b.*?(\\d+)/(\\d+)\\b"),
            MountStat.SPEED, Pattern.compile("\\bSpeed\\b.*?(\\d+)/(\\d+)\\b"),
            MountStat.TOUGHNESS, Pattern.compile("\\bToughness\\b.*?(\\d+)/(\\d+)\\b"),
            MountStat.TRAINING, Pattern.compile("\\bTraining\\b.*?(\\d+)/(\\d+)\\b"));

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.POTION) return null;

        Matcher matcher = name.getMatcher(MOUNT_PATTERN);
        if (!matcher.find()) return null;

        List<StyledText> lore = LoreUtils.getLore(itemStack);
        List<String> plainLore =
                lore.stream().map(StyledText::getStringWithoutFormatting).toList();

        ParsedMountStats stats = parseMountStats(plainLore);
        String mountName = parseMountName(name.getStringWithoutFormatting()).orElse(null);

        return new MountItem(
                mountName,
                stats.value(MountStat.ENERGY),
                stats.value(MountStat.ACCELERATION),
                stats.value(MountStat.ALTITUDE),
                stats.value(MountStat.ENERGY),
                stats.value(MountStat.HANDLING),
                stats.value(MountStat.POWERUP),
                stats.value(MountStat.SPEED),
                stats.value(MountStat.TOUGHNESS),
                stats.value(MountStat.TRAINING));
    }

    private ParsedMountStats parseMountStats(List<String> lines) {
        Map<MountStat, CappedValue> statValues = new EnumMap<>(MountStat.class);

        for (String line : lines) {
            for (Map.Entry<MountStat, Pattern> entry : STAT_PATTERNS.entrySet()) {
                MountStat stat = entry.getKey();
                if (stat != MountStat.ENERGY && statValues.containsKey(stat)) continue;

                Matcher statMatcher = entry.getValue().matcher(line);
                if (statMatcher.find()) {
                    // Energy appears multiple times in lore; keep the latest matching line (the mount stat line).
                    statValues.put(stat, parseCapped(statMatcher));
                }
            }
        }

        return new ParsedMountStats(statValues);
    }

    private CappedValue parseCapped(Matcher matcher) {
        int current = Integer.parseInt(matcher.group(1));
        int max = Integer.parseInt(matcher.group(2));
        return new CappedValue(current, max);
    }

    private record ParsedMountStats(Map<MountStat, CappedValue> statValues) {
        private CappedValue value(MountStat stat) {
            return statValues.getOrDefault(stat, CappedValue.EMPTY);
        }
    }

    private Optional<String> parseMountName(String plainName) {
        Matcher matcher = MOUNT_PATTERN.matcher(plainName);
        if (!matcher.find()) return Optional.empty();

        String value = matcher.group(1).trim();
        if (value.endsWith("'s") || value.endsWith("'S")) {
            value = value.substring(0, value.length() - 2).trim();
        }

        if (value.endsWith("'")) {
            value = value.substring(0, value.length() - 1).trim();
        }
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }
}
