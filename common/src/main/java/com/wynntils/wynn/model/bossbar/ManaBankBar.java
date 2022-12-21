/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.bossbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.wynn.objects.ClassType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ManaBankBar extends TrackedBar {
    public ManaBankBar() {
        super(Pattern.compile("§bMana Bank §3\\[(\\d+)/(\\d+)§3\\]"), BarType.MANABANK, ClassType.Mage);
    }

    @Override
    public void onUpdateName(Matcher match) {
        try {
            current = Integer.parseInt(match.group(1));
            max = Integer.parseInt(match.group(2));
        } catch (NumberFormatException e) {
            WynntilsMod.error(String.format(
                    "Failed to parse current and max for mana bank bar %s (%s out of %s)",
                    type, match.group(1), match.group(2)));
        }
    }
}
