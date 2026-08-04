/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountInfo;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MountAnnotator implements GameItemAnnotator {
    private static final Pattern MOUNT_PATTERN = Pattern.compile(
            "\uDAFC\uDC00(?<name>.+?)(?:'s?)? (?<type>Saddle|Reins|Harness|Whistle|Flute|Ocarina)\uDAFC\uDC00");
    private static final Pattern POTENTIAL_PATTERN =
            Pattern.compile("§f\uDB00\uDC01§#e0e0e0ff(?<potential>(?:\\d+(?:\\.\\d+)?k?|\\d+))§f Potential");
    private static final Pattern COLOR_PATTERN =
            Pattern.compile("§f\uE00E\uDB00\uDC01§7 (?<primaryColor>.+)-(?<secondaryColor>.+)");
    private static final Pattern ENERGY_PATTERN =
            Pattern.compile("§8\uE023\uDAFF\uDFF7§#e0e0e0ff.§7\uDB00\uDC05Energy (?<current>\\d+)/(?<cap>\\d+)");
    private static final Pattern STAT_PATTERN = Pattern.compile(
            "§f(?<statName>"
                    + Arrays.stream(MountStat.values())
                            .map(s -> Pattern.quote(s.getName()))
                            .collect(Collectors.joining("|"))
                    + ").+§#acfac6ff(?<current>\\d+)§7/(?<cap>\\d+)( §8\\((?<max>\\d+)\\))?(?:§f|§#acfac6ff) §8\uE023\uDAFF\uDFF7.+");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.POTION) return null;

        Matcher matcher = name.getMatcher(MOUNT_PATTERN);
        if (!matcher.matches()) return null;

        String type = matcher.group("type");
        MountType mountType = MountType.fromName(type);

        if (mountType == null) {
            WynntilsMod.warn("Unknown mount type " + type);
            return null;
        }

        boolean isSummonItem = mountType.getSummonItemName().equals(type);
        List<StyledText> lore = LoreUtils.getLore(itemStack);

        MountInfo info = parseMountInfo(lore);

        return new MountItem(matcher.group("name"), mountType, info, isSummonItem);
    }

    private MountInfo parseMountInfo(List<StyledText> lore) {
        int potential = -1;
        Optional<String> primaryColor = Optional.empty();
        Optional<String> secondaryColor = Optional.empty();
        CappedValue currentEnergy = CappedValue.EMPTY;
        Map<MountStat, CappedValue> stats = new EnumMap<>(MountStat.class);
        Map<MountStat, Integer> maxStats = new EnumMap<>(MountStat.class);

        for (StyledText line : lore) {
            Matcher matcher = line.getMatcher(POTENTIAL_PATTERN);
            if (potential == -1 && matcher.matches()) {
                potential = (int) StringUtils.parseSuffixedInteger(matcher.group("potential"));
                continue;
            }

            matcher = line.getMatcher(COLOR_PATTERN);
            if (primaryColor.isEmpty() && matcher.matches()) {
                primaryColor = Optional.of(matcher.group("primaryColor"));
                secondaryColor = Optional.of(matcher.group("secondaryColor"));
                continue;
            }

            matcher = line.getMatcher(ENERGY_PATTERN);
            if (currentEnergy == CappedValue.EMPTY && matcher.matches()) {
                currentEnergy = parseCapped(matcher);
                continue;
            }

            matcher = line.getMatcher(STAT_PATTERN);
            if (matcher.matches()) {
                Optional<MountStat> stat = MountStat.fromName(matcher.group("statName"));

                if (stat.isPresent()) {
                    stats.put(stat.get(), parseCapped(matcher));

                    if (matcher.group("max") != null) {
                        maxStats.put(stat.get(), Integer.parseInt(matcher.group("max")));
                    }
                }
            }
        }

        return new MountInfo(potential, primaryColor, secondaryColor, currentEnergy, stats, maxStats);
    }

    private CappedValue parseCapped(Matcher matcher) {
        int current = Integer.parseInt(matcher.group("current"));
        int cap = Integer.parseInt(matcher.group("cap"));
        return new CappedValue(current, cap);
    }
}
