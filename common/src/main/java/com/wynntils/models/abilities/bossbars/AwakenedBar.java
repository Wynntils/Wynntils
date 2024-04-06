/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AwakenedBar extends TrackedBar {
    private static final Pattern AWAKENED_PATTERN = Pattern.compile("§fAwakening §7\\[§f(\\d+)/(\\d+)§7]");

    public AwakenedBar() {
        super(AWAKENED_PATTERN);
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            int current = Integer.parseInt(match.group(1));
            int max = Integer.parseInt(match.group(2));
            updateValue(current, max);
        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format(
                    "Failed to parse current and max for awakened bar (%s out of %s)", match.group(1), match.group(2)));
        }
    }
}
