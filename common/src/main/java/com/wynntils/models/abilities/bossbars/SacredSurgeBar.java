/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Pattern;

public class SacredSurgeBar extends TrackedBar {
    private static final Pattern SACRED_SURGE_PATTERN = Pattern.compile("§bSacred Surge §3\\[§b(\\d+)%§3\\]");

    public SacredSurgeBar() {
        super(SACRED_SURGE_PATTERN);
    }

    @Override
    public void onUpdateProgress(float progress) {
        updateValue((int) (progress * 100), 100);
    }
}
