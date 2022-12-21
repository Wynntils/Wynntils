/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.bossbar;

import com.wynntils.core.managers.Handlers;
import com.wynntils.core.managers.Model;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.Arrays;
import java.util.List;

public final class BossBarModel extends Model {
    public static final TrackedBar manaBankBar = new ManaBankBar();

    public static final TrackedBar bloodPoolBar = new BloodPoolBar();

    public static final TrackedBar awakenedBar = new AwakenedBar();

    public static final TrackedBar focusBar = new FocusBar();

    public static final TrackedBar corruptedBar = new CorruptedBar();

    public static final List<TrackedBar> ALL_BARS =
            Arrays.asList(manaBankBar, bloodPoolBar, awakenedBar, focusBar, corruptedBar);

    @Override
    public void init() {
        ALL_BARS.forEach(Handlers.BossBar::registerBar);
    }

    @Override
    public void disable() {
        ALL_BARS.forEach(Handlers.BossBar::unregisterBar);
    }
}
