/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FocusBar extends TrackedBar {
    private static final Pattern FOCUS_PATTERN = Pattern.compile("§eFocus §6\\[§e(\\d+)/(\\d+)§6]");

    public FocusBar() {
        super(FOCUS_PATTERN);
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            int current = Integer.parseInt(match.group(1));
            int max = Integer.parseInt(match.group(2));
            updateValue(current, max);
        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format(
                    "Failed to parse current and max for focus bar (%s out of %s)", match.group(1), match.group(2)));
        }
    }
}
