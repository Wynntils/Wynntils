/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ManaBankBar extends TrackedBar {
    private static final Pattern MANA_BANK_PATTERN = Pattern.compile("§bMana Bank §3\\[(\\d+)/(\\d+)\\]");

    public ManaBankBar() {
        super(MANA_BANK_PATTERN);
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            int current = Integer.parseInt(match.group(1));
            int max = Integer.parseInt(match.group(2));
            updateValue(current, max);
        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format(
                    "Failed to parse current and max for mana bank bar (%s out of %s)",
                    match.group(1), match.group(2)));
        }
    }
}
