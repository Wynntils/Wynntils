/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class ClientTickEvent extends Event {
    public static class Start extends ClientTickEvent {}

    public static class End extends ClientTickEvent {}
}
