/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.bossbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AwakenedBar extends TrackedBar {
    public AwakenedBar() {
        super(Pattern.compile("§fAwakening §7\\[§f(\\d+)/(\\d+)§7]"), BossBarModel.AWAKENED);
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            current = Integer.parseInt(match.group(1));
            max = Integer.parseInt(match.group(2));
        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format(
                    "Failed to parse current and max for awakened bar (%s out of %s)",
                    match.group(1), match.group(2)));
        }
    }
}
