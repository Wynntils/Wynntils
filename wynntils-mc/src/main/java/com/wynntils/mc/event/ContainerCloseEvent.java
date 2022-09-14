/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class ContainerCloseEvent extends Event {
    @Cancelable
    public static class Pre extends ContainerCloseEvent {}

    public static class Post extends ContainerCloseEvent {}
}
