/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.abilities.bossbars.AwakenedBar;
import com.wynntils.models.abilities.bossbars.BloodPoolBar;
import com.wynntils.models.abilities.bossbars.CommanderBar;
import com.wynntils.models.abilities.bossbars.CorruptedBar;
import com.wynntils.models.abilities.bossbars.FocusBar;
import com.wynntils.models.abilities.bossbars.HolyPowerBar;
import com.wynntils.models.abilities.bossbars.ManaBankBar;
import com.wynntils.models.abilities.bossbars.OphanimBar;
import java.util.Arrays;
import java.util.List;

public final class AbilityModel extends Model {
    public static final TrackedBar manaBankBar = new ManaBankBar();

    public static final TrackedBar bloodPoolBar = new BloodPoolBar();

    public static final TrackedBar awakenedBar = new AwakenedBar();

    public static final TrackedBar focusBar = new FocusBar();

    public static final TrackedBar corruptedBar = new CorruptedBar();

    public static final OphanimBar ophanimBar = new OphanimBar();

    public static final TrackedBar holyPowerBar = new HolyPowerBar();

    public static final CommanderBar commanderBar = new CommanderBar();

    private static final List<TrackedBar> ALL_BARS = Arrays.asList(
            manaBankBar, bloodPoolBar, awakenedBar, focusBar, corruptedBar, ophanimBar, holyPowerBar, commanderBar);

    public AbilityModel() {
        super(List.of());

        ALL_BARS.forEach(Handlers.BossBar::registerBar);
    }
}
