/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class GuildWarEvent extends Event {
    /**
     * Event fired when a guild war starts, when the tower starts attacking.
     */
    public static class Started extends GuildWarEvent {}

    /**
     * Event fired when a guild war ends, when the tower stops attacking.
     * Note that leaving/dying/capturing territories all call this event.
     */
    public static class Ended extends GuildWarEvent {}
}
