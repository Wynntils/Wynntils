/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.bossbars;

import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Pattern;

public class AnnihilationSunBar extends TrackedBar {
    private static final Pattern SUN_PATTERN = Pattern.compile("^§cForming a New Sun...$");

    public AnnihilationSunBar() {
        super(SUN_PATTERN);
    }

    @Override
    public void onUpdateProgress(float progress) {
        updateValue((int) (progress * 100), 100);
    }
}
