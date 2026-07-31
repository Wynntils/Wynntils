/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DistortionBar extends TrackedBar {
    private static final Pattern DISTORTION_PATTERN = Pattern.compile("§#d599ffff\uE035 Distortion: §b(\\d+)");
    private int current;

    public DistortionBar() {
        super(DISTORTION_PATTERN);
    }

    public int getCurrent() {
        return current;
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            current = Integer.parseInt(match.group(1));
        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format("Failed to parse current for distortion bar (%s)", match.group(1)));
        }
    }

    @Override
    public void reset() {
        super.reset();

        current = 0;
    }
}
