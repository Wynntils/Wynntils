/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.discoveries.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class DiscoveriesUpdatedEvent extends Event {
    public static class Territory extends DiscoveriesUpdatedEvent {}

    public static class World extends DiscoveriesUpdatedEvent {}

    public static class Secret extends DiscoveriesUpdatedEvent {}
}
