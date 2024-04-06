/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BloodPoolBar extends TrackedBar {
    private static final Pattern BLOOD_POOL_PATTERN = Pattern.compile("§cBlood Pool §4\\[§c(\\d+)%§4\\]");
    private int current;

    public BloodPoolBar() {
        super(BLOOD_POOL_PATTERN);
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

            int max = unroundedMax - remainder;
            if (remainder > 15) {
                max += 30;
            }
            updateValue(current, max);
        }
    }
}
