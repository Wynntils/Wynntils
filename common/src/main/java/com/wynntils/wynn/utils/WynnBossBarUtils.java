/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.Pair;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.LerpingBossEvent;

public class WynnBossBarUtils {
    public static final BarProgress NO_MANA_BANK = new BarProgress(0, 0, -1);
    public static final BarProgress NO_BLOOD_POOL = new BarProgress(0, 0, -1);
    public static final BarProgress NO_AWAKENED_PROGRESS = new BarProgress(0, 0, -1);
    public static final BarProgress NO_FOCUS = new BarProgress(0, 0, -1);

    private static final Pattern BLOOD_POOL_PATTERN = Pattern.compile("§cBlood Pool §4\\[§c(\\d+)%§4\\]");
    private static final Pattern MANA_BANK_PATTERN = Pattern.compile("§bMana Bank §3\\[(\\d+)/(\\d+)§3\\]");
    private static final Pattern AWAKENED_PROGRESS_PATTERN = Pattern.compile("§fAwakening §7\\[§f(\\d+)/(\\d+)§7]");
    private static final Pattern FOCUS_PATTERN = Pattern.compile("§eFocus §6\\[§e(\\d+)/(\\d+)§6]");

    private static Pair<LerpingBossEvent, Matcher> getLerpingBossEvent(Pattern titlePattern) {
        LerpingBossEvent poolEvent = null;
        Matcher matcher = null;

        Collection<LerpingBossEvent> events =
                McUtils.mc().gui.getBossOverlay().events.values();

        for (LerpingBossEvent event : events) {
            matcher = titlePattern.matcher(ComponentUtils.getCoded(event.getName()));
            if (matcher.matches()) {
                poolEvent = event;
                break;
            }
        }

        return new Pair<>(poolEvent, matcher);
    }

    public static BarProgress getManaBank() {
        Pair<LerpingBossEvent, Matcher> pair = getLerpingBossEvent(MANA_BANK_PATTERN);

        if (pair.a == null) return NO_MANA_BANK;

        try {
            int manaBankPercent = Integer.parseInt(pair.b.group(1));
            int manaBankMaxPercent = Integer.parseInt(pair.b.group(2));
            float progress = pair.a.getProgress();

            return new BarProgress(manaBankPercent, manaBankMaxPercent, progress);
        } catch (NumberFormatException e) {
            return NO_MANA_BANK;
        }
    }

    public static BarProgress getBloodPool() {
        Pair<LerpingBossEvent, Matcher> pair = getLerpingBossEvent(BLOOD_POOL_PATTERN);

        if (pair.a == null) return NO_BLOOD_POOL;

        try {
            int currentBloodPool = Integer.parseInt(pair.b.group(1));
            float progress = pair.a.getProgress();
            int maxBloodPool;
            if (progress == 0f) {
                maxBloodPool = -1;
            } else {
                // Round to nearest 30
                maxBloodPool = Math.round(currentBloodPool / (progress * 30f)) * 30;
            }

            return new BarProgress(currentBloodPool, maxBloodPool, progress);
        } catch (NumberFormatException e) {
            return NO_BLOOD_POOL;
        }
    }

    public static BarProgress getAwakenedBar() {
        Pair<LerpingBossEvent, Matcher> pair = getLerpingBossEvent(AWAKENED_PROGRESS_PATTERN);

        if (pair.a == null) return NO_AWAKENED_PROGRESS;

        try {
            int currentAwakenedProgress = Integer.parseInt(pair.b.group(1));
            int maxAwakenedProgress = Integer.parseInt(pair.b.group(2));
            float progress = pair.a.getProgress();

            return new BarProgress(currentAwakenedProgress, maxAwakenedProgress, progress);
        } catch (NumberFormatException e) {
            return NO_AWAKENED_PROGRESS;
        }
    }

    public static BarProgress getFocusBar() {
        Pair<LerpingBossEvent, Matcher> pair = getLerpingBossEvent(FOCUS_PATTERN);

        if (pair.a == null) return NO_FOCUS;

        try {
            int currentFocus = Integer.parseInt(pair.b.group(1));
            int maxFocus = Integer.parseInt(pair.b.group(2));
            float progress = pair.a.getProgress();

            return new BarProgress(currentFocus, maxFocus, progress);
        } catch (NumberFormatException e) {
            return NO_FOCUS;
        }
    }

    public record BarProgress(int current, int max, float progress) {}
}
