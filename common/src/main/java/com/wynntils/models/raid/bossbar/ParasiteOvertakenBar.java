/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.bossbar;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Pattern;

public class ParasiteOvertakenBar extends TrackedBar {
    // Test in ParasiteOvertakenBar_OVERTAKEN_PATTERN
    private static final Pattern OVERTAKEN_PATTERN =
            Pattern.compile("(§#aa00ffff)?.*(§8.*)?§r\uDAFF\uDF81§fOVERTAKEN\uDB00\uDC49");

    public ParasiteOvertakenBar() {
        super(OVERTAKEN_PATTERN);
    }

    @Override
    protected void reset() {
        super.reset();

        Models.Raid.resetParasiteOvertaken();
    }
}
