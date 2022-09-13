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

    public static Pair<LerpingBossEvent, Matcher> getLerpingBossEvent(Pattern titlePattern) {
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
}
