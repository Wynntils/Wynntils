/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.bossbar;

import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Pattern;

public class DeathScreenBar extends TrackedBar {
    private static final Pattern DEATH_BAR_PATTERN = Pattern.compile("^§#261f1fff\uE000\uE002\uE000$");

    public DeathScreenBar() {
        super(DEATH_BAR_PATTERN);
    }
}
