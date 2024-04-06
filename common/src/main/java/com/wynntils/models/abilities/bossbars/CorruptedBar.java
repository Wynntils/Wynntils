/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CorruptedBar extends TrackedBar {
    private static final Pattern CORRUPTED_PATTERN = Pattern.compile("§cCorrupted §4\\[§c(\\d+)%§4]");

    public CorruptedBar() {
        super(CORRUPTED_PATTERN);
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            int current = Integer.parseInt(match.group(1));
            int max = 100;
            updateValue(current, max);
        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format(
                    "Failed to parse current and max for corrupted bar (%s out of %s)",
                    match.group(1), match.group(2)));
        }
    }
}
