/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NightcloakKnivesBar extends TrackedBar {
    private static final Pattern NIGHTCLOAK_KNIVES_PATTERN = Pattern.compile("§d(\\d+) Nightcloak Knives");
    private int current;

    public NightcloakKnivesBar() {
        super(NIGHTCLOAK_KNIVES_PATTERN);
    }

    public int getCurrent(){return current;}

    @Override
    public void onUpdateName(Matcher match) {
        try {
            current = Integer.parseInt(match.group(1));

        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format(
                    "Failed to parse current nightcloak knife armount (%s)", match.group(1)));
        }
    }
}