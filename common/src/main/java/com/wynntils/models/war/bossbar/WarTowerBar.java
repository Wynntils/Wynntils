/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war.bossbar;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.war.type.WarTowerState;
import com.wynntils.utils.type.RangedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WarTowerBar extends TrackedBar {
    // Test in WarTowerBar_TOWER_BAR_PATTERN
    private static final Pattern TOWER_BAR_PATTERN = Pattern.compile(
            "§3\\[(?<guild>.+)\\] §b(?<territory>.+) Tower§7 - §4. (?<health>.+)§7 \\(§6(?<defense>.+)%§7\\) - §..{1,2} (?<damagemin>.+)-(?<damagemax>.+)§7 \\(§b(?<attackspeed>.+)x§7\\)");

    public WarTowerBar() {
        super(TOWER_BAR_PATTERN);
    }

    @Override
    public void onUpdateName(Matcher match) {
        WarTowerState towerState = new WarTowerState(
                Long.parseLong(match.group("health")),
                Double.parseDouble(match.group("defense")),
                RangedValue.of(Integer.parseInt(match.group("damagemin")), Integer.parseInt(match.group("damagemax"))),
                Double.parseDouble(match.group("attackspeed")),
                System.currentTimeMillis());

        Models.GuildWarTower.onTowerDamaged(match.group("guild"), match.group("territory"), towerState);
    }

    @Override
    protected void reset() {
        super.reset();

        // War ended for some reason, reset the tower state
        Models.GuildWarTower.resetTowerState();
    }
}
