/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.Pair;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.LerpingBossEvent;

public class WynnBossBarUtils {
    public static final ManaBank NO_MANA_BANK = new ManaBank(0, 0, -1);
    public static final BloodPool NO_BLOOD_POOL = new BloodPool(0, -1);

    private static final Pattern BLOOD_POOL_PATTERN = Pattern.compile("§cBlood Pool §4\\[§c(\\d+)%§4\\]");
    private static final Pattern MANA_BANK_PATTERN = Pattern.compile("§bMana Bank §3\\[(\\d+)/(\\d+)§3\\]");

    private static Pair<LerpingBossEvent, Matcher> getLerpingBossEvent(Pattern titlePattern) {
        LerpingBossEvent poolEvent = null;
        Matcher matcher = null;

        for (LerpingBossEvent event : McUtils.mc().gui.getBossOverlay().events.values()) {
            matcher = titlePattern.matcher(ComponentUtils.getCoded(event.getName()));
            if (matcher.matches()) {
                poolEvent = event;
                break;
            }
        }

        return new Pair<>(poolEvent, matcher);
    }

    public static ManaBank getManaBank() {
        Pair<LerpingBossEvent, Matcher> pair = getLerpingBossEvent(MANA_BANK_PATTERN);

        if (pair.a == null) return NO_MANA_BANK;

        try {
            int manaBankPercent = Integer.parseInt(pair.b.group(1));
            int manaBankMaxPercent = Integer.parseInt(pair.b.group(2));
            float progress = pair.a.getProgress();

            return new ManaBank(manaBankPercent, manaBankMaxPercent, progress);
        } catch (NumberFormatException e) {
            return NO_MANA_BANK;
        }
    }

    public static BloodPool getBloodPool() {
        Pair<LerpingBossEvent, Matcher> pair = getLerpingBossEvent(BLOOD_POOL_PATTERN);

        if (pair.a == null) return NO_BLOOD_POOL;

        try {
            int manaBankPercent = Integer.parseInt(pair.b.group(1));
            float progress = pair.a.getProgress();

            return new BloodPool(manaBankPercent, progress);
        } catch (NumberFormatException e) {
            return NO_BLOOD_POOL;
        }
    }

    public record ManaBank(int percent, int maxPercent, float progress) {}

    public record BloodPool(int percent, float progress) {}
}
