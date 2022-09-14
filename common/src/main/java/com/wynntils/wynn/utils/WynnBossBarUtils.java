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

    public static Pair<LerpingBossEvent, Pair<String, String>> getManaBankEvent() {
        Pair<LerpingBossEvent, Matcher> pair = getLerpingBossEvent(MANA_BANK_PATTERN);

        return new Pair<>(pair.a, new Pair<>(pair.b.group(1), pair.b.group(2)));
    }

    public static Pair<LerpingBossEvent, String> getBloodPoolEvent() {
        Pair<LerpingBossEvent, Matcher> pair = getLerpingBossEvent(BLOOD_POOL_PATTERN);

        return new Pair<>(pair.a, pair.b.group(1));
    }
}
