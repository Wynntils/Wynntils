/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.models.territories.TerritoryAttackTimer;

public final class GuildWarQueuedEvent extends BaseEvent {
    private final TerritoryAttackTimer attackTimer;

    public GuildWarQueuedEvent(TerritoryAttackTimer attackTimer) {
        this.attackTimer = attackTimer;
    }

    public TerritoryAttackTimer getAttackTimer() {
        return attackTimer;
    }
}
