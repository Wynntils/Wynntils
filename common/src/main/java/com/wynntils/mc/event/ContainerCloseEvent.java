/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public abstract class ContainerCloseEvent extends WynntilsEvent {
    @Cancelable
    public static class Pre extends ContainerCloseEvent {}

    public static class Post extends ContainerCloseEvent {}
}
