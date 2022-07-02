/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class DisplayResizeEvent extends Event {
    public static class Pre extends DisplayResizeEvent {}

    public static class Post extends DisplayResizeEvent {}
}
