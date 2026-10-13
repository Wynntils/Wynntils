/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.event;

import net.neoforged.bus.api.Event;

public abstract class LootrunChallengeEvent extends Event {
    public static class Completed extends LootrunChallengeEvent {}

    public static class Failed extends LootrunChallengeEvent {}
}
