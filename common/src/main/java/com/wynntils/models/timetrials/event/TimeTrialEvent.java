/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials.event;

import net.neoforged.bus.api.Event;

public abstract class TimeTrialEvent extends Event {
    public static class Ready extends TimeTrialEvent {}

    public static class Started extends TimeTrialEvent {}

    public static class Resumed extends TimeTrialEvent {}

    public static class Failed extends TimeTrialEvent {}

    public static class Cleared extends TimeTrialEvent {}
}
