/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import com.wynntils.wynn.event.StatusEffectsChangedEvent;
import com.wynntils.wynn.objects.timers.StaticStatusTimer;
import com.wynntils.wynn.objects.timers.StatusTimer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class TabModel extends Model {
    /**
     * CG1 is the color and symbol used for the effect, and the strength modifier string (e.g. "79%")
     * NCG1 is for strength modifiers without a decimal, and the % sign
     * NCG2 is the decimal point and second \d+ option for strength modifiers with a decimal
     * CG2 is the actual name of the effect
     * CG3 is the duration string (eg. "1:23")
     * Note: Buffs like "+190 Main Attack Damage" will have the +190 be considered as part of the name.
     * Buffs like "17% Frenzy" will have the 17% be considered as part of the prefix.
     * This is because the 17% in Frenzy (and certain other buffs) can change, but the static scroll buffs cannot.
     *
     * <p>Originally taken from: <a href="https://github.com/Wynntils/Wynntils/pull/615">Legacy</a>
     */
    private static final Pattern TAB_EFFECT_PATTERN =
            Pattern.compile("(.+?§7 ?(?:\\d+(?:\\.\\d+)?%)?) ?([%\\-+\\/\\da-zA-Z'\\s]+?) §[84a]\\((.+?)\\).*");

    private static final String STATUS_EFFECTS_TITLE = "§d§lStatus Effects";

    private static List<StatusTimer> timers = new ArrayList<>();

    public static void init() {}

    @SubscribeEvent
    public static void onTabListCustomization(PlayerInfoFooterChangedEvent event) {
        String footer = event.getFooter();

        if (footer.isEmpty()) {
            if (!timers.isEmpty()) {
                timers = new ArrayList<>(); // No timers, get rid of them
                WynntilsMod.postEvent(new StatusEffectsChangedEvent());
            }

            return;
        }

        if (!footer.startsWith(STATUS_EFFECTS_TITLE)) return;

        List<StatusTimer> newTimers = new ArrayList<>();

        String[] effects = footer.split("\\s{2}"); // Effects are split up by 2 spaces
        for (String effect : effects) {
            effect = effect.trim();
            if (effect.isEmpty()) continue;

            Matcher m = TAB_EFFECT_PATTERN.matcher(effect);
            if (!m.find()) continue;

            // See comment at TAB_EFFECT_PATTERN definition for what group numbers are
            newTimers.add(new StaticStatusTimer(m.group(1), m.group(2), m.group(3)));
        }

        timers = newTimers;
        WynntilsMod.postEvent(new StatusEffectsChangedEvent());
    }

    public static List<StatusTimer> getTimers() {
        return timers;
    }
}
