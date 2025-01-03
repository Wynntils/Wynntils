/*
 * Copyright © Wynntils 2022-2024.
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
            WynntilsMod.error(String.format("Failed to parse current for blood pool bar (%s)", match.group(1)));
        }
    }

    // Wynncraft sends the name packet before the progress packet
    @Override
    public void onUpdateProgress(float progress) {
        if (progress != 0f) {
            // Round to nearest 10
            int unroundedMax = (int) (current / progress);
            int remainder = unroundedMax % 10;

            int max = unroundedMax - remainder;
            if (remainder > 5) {
                max += 10;
            }
            updateValue(current, max);
        }
    }
}
