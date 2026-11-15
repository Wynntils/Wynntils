/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnTheBrinkBar extends TrackedBar {
    private static final Pattern ON_THE_BRINK_PATTERN = Pattern.compile("§cOn the Brink: (?<onTheBrink>\\d+) §r\uE008");

    // This bar gets added with the name "Unknown" and then changed to the actual text.
    private static final Pattern UNKNOWN_PATTERN = Pattern.compile("Unknown");

    private int onTheBrink = 0;

    public OnTheBrinkBar() {
        super(List.of(ON_THE_BRINK_PATTERN, UNKNOWN_PATTERN));
    }

    public int getOnTheBrink() {
        return onTheBrink;
    }

    @Override
    public void onUpdateName(Matcher match) {
        if (match.pattern() != ON_THE_BRINK_PATTERN) return;

        onTheBrink = Integer.parseInt(match.group("onTheBrink"));
    }

    @Override
    public void onUpdateProgress(float progress) {
        updateValue((int) (progress * 100), 100);
    }
}
