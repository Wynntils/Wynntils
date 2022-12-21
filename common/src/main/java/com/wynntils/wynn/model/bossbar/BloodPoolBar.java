/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.bossbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.wynn.objects.ClassType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BloodPoolBar extends TrackedBar {
    public BloodPoolBar() {
        super(Pattern.compile("§cBlood Pool §4\\[§c(\\d+)%§4\\]"), BarType.BLOODPOOL, ClassType.Shaman);
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            current = Integer.parseInt(match.group(1));
        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format(
                    "Failed to parse current for blood pool bar (%s out of %s)", match.group(1), match.group(2)));
        }
    }

    // Wynncraft sends the name packet before the progress packet
    @Override
    public void onUpdateProgress(float progress) {
        if (progress != 0f) {
            // Round to nearest 30
            int unroundedMax = (int) (current / progress);
            int remainder = unroundedMax % 30;

            max = unroundedMax - remainder;
            if (remainder > 15) {
                max += 30;
            }
        }
    }
}
