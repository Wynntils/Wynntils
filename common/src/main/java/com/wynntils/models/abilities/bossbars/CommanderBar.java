/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommanderBar extends TrackedBar {
    private static final Pattern COMMANDER_PATTERN = Pattern.compile("§(c|a)Commander: ([0-9]+)s");

    private int duration = 0;
    private boolean activated = false;

    public CommanderBar() {
        super(COMMANDER_PATTERN);
    }

    public int getDuration() {
        return duration;
    }

    public boolean isActivated() {
        return activated;
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            activated = match.group(1).equals("a");
            duration = Integer.parseInt(match.group(2));
        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format(
                    "Failed to parse active state and duration for commander bar (%b, %ds)", activated, duration));
        }
    }
}
