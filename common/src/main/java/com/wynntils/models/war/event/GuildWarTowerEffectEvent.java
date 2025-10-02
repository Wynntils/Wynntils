/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war.event;

import com.wynntils.core.events.BaseEvent;

public abstract class GuildWarTowerEffectEvent extends BaseEvent {
    public static class AuraSpawned extends GuildWarTowerEffectEvent {}

    public static class VolleySpawned extends GuildWarTowerEffectEvent {}
}
