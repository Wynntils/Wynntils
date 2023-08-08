/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.abilities.bossbars.AwakenedBar;
import com.wynntils.models.abilities.bossbars.BloodPoolBar;
import com.wynntils.models.abilities.bossbars.CorruptedBar;
import com.wynntils.models.abilities.bossbars.FocusBar;
import com.wynntils.models.abilities.bossbars.ManaBankBar;
import java.util.Arrays;
import java.util.List;

public final class BossBarModel extends Model {
    public static final TrackedBar manaBankBar = new ManaBankBar();

    public static final TrackedBar bloodPoolBar = new BloodPoolBar();

    public static final TrackedBar awakenedBar = new AwakenedBar();

    public static final TrackedBar focusBar = new FocusBar();

    public static final TrackedBar corruptedBar = new CorruptedBar();

    private static final List<TrackedBar> ALL_BARS =
            Arrays.asList(manaBankBar, bloodPoolBar, awakenedBar, focusBar, corruptedBar);

    public BossBarModel() {
        super(List.of());

        ALL_BARS.forEach(Handlers.BossBar::registerBar);
    }
}
