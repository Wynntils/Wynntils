/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.bossbar;

import com.wynntils.core.managers.Model;
import com.wynntils.handlers.bossbar.BossBarHandler;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import java.util.Arrays;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossBarModel extends Model {
    public static final TrackedBar manaBankBar = new ManaBankBar();

    public static final TrackedBar bloodPoolBar = new BloodPoolBar();

    public static final TrackedBar awakenedBar = new AwakenedBar();

    public static final TrackedBar focusBar = new FocusBar();

    public static final TrackedBar corruptedBar = new CorruptedBar();

    public static final List<TrackedBar> KNOWN_BARS =
            Arrays.asList(manaBankBar, bloodPoolBar, awakenedBar, focusBar, corruptedBar);

    @SubscribeEvent(receiveCanceled = true)
    public void onHealthBarEvent(BossHealthUpdateEvent event) {
        // For now, be a proxy for the BossBarHandler
        BossBarHandler.INSTANCE.onHealthBarEvent(event);
    }
}
