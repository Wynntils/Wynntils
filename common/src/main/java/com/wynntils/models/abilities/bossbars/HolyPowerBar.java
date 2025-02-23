/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Pattern;

public class HolyPowerBar extends TrackedBar {
    private static final Pattern HOLY_POWER_PATTERN = Pattern.compile("§bHoly Power §3\\[§b(\\d+)%§3\\]");

    public HolyPowerBar() {
        super(HOLY_POWER_PATTERN);
    }

    @Override
    public void onUpdateProgress(float progress) {
        updateValue((int) (progress * 100), 100);
    }
}
