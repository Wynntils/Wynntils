/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.discoveries.event;

import com.wynntils.core.events.WynntilsEvent;

public abstract class DiscoveriesUpdatedEvent extends WynntilsEvent {
    public static class Normal extends DiscoveriesUpdatedEvent {}

    public static class Secret extends DiscoveriesUpdatedEvent {}
}
