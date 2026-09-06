/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.event;

import com.wynntils.utils.type.CappedValue;
import net.neoforged.bus.api.Event;

public class LootrunChallengeCountEvent extends Event {
    private final CappedValue newCount;
    private final CappedValue oldCount;

    public LootrunChallengeCountEvent(CappedValue newCount, CappedValue oldCount) {
        this.newCount = newCount;
        this.oldCount = oldCount;
    }

    public CappedValue getNewCount() {
        return newCount;
    }

    public CappedValue getOldCount() {
        return oldCount;
    }
}
