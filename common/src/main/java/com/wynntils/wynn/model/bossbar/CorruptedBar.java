/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.bossbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CorruptedBar extends TrackedBar {
    public CorruptedBar() {
        super(Pattern.compile("§cCorrupted §4\\[§c(\\d+)%§4]"), BossBarModel.CORRUPTED);
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            current = Integer.parseInt(match.group(1));
            max = 100;
        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format(
                    "Failed to parse current and max for corrupted bar (%s out of %s)",
                    match.group(1), match.group(2)));
        }
    }
}
