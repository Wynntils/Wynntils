/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MomentumBar extends TrackedBar {
    private static final Pattern MOMENTUM_PATTERN =
            Pattern.compile("§f(?<momentum>\\d+)§7 Momentum(?<max> §8\\[§fMAX§8\\])?");

    private int momentum = 0;
    private boolean max = false;

    public MomentumBar() {
        super(MOMENTUM_PATTERN);
    }

    public int getMomentum() {
        return momentum;
    }

    public boolean isMax() {
        return max;
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            max = match.group("max") != null;
            momentum = Integer.parseInt(match.group("momentum"));
        } catch (NumberFormatException e) {
            WynntilsMod.error(
                    String.format("Failed to parse momentum count for momentum bar (%s)", match.group("momentum")));
        }
    }

    @Override
    public void onUpdateProgress(float progress) {
        updateValue((int) (progress * 100), 100);
    }
}
