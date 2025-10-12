/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.event;

import net.neoforged.bus.api.Event;

public abstract class AnnihilationEvent extends Event {
    protected AnnihilationEvent() {}

    public static class Completed extends AnnihilationEvent {}

    public static class Failed extends AnnihilationEvent {}
}
