/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.parsers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.objects.EmeraldSymbols;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

/** Tools for retrieving information about emerald pouches */
public final class EmeraldPouchParser {
    private static final Pattern POUCH_USAGE_PATTERN =
            Pattern.compile("§6§l(\\d* ?\\d* ?\\d*)" + EmeraldSymbols.E_STRING);
    private static final Pattern POUCH_CAPACITY_PATTERN =
            Pattern.compile("\\((\\d+)(" + EmeraldSymbols.EB + "|" + EmeraldSymbols.LE + "|stx) Total\\)");

    public static int getPouchUsage(ItemStack stack) {
        String lore = ItemUtils.getStringLore(stack);
        Matcher usageMatcher = POUCH_USAGE_PATTERN.matcher(lore);
        if (!usageMatcher.find()) {
            return 0;
        }
        return Integer.parseInt(usageMatcher.group(1).replaceAll("\\s", ""));
    }

    public static int getPouchCapacity(ItemStack stack) {
        String lore = ItemUtils.getStringLore(stack);
        Matcher capacityMatcher = POUCH_CAPACITY_PATTERN.matcher(lore);
        if (!capacityMatcher.find()) {
            WynntilsMod.error(
                    "EmeraldPouchParser#getPouchCapacity was called on an ItemStack that wasn't an emerald pouch");
            return -1;
        }
        int capacity = Integer.parseInt(capacityMatcher.group(1)) * 64;
        if (capacityMatcher.group(2).equals(EmeraldSymbols.LE)) capacity *= 64;
        if (capacityMatcher.group(2).equals("stx")) capacity *= 4096;
        return capacity;
    }
}
