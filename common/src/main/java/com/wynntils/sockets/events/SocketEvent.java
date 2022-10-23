/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.sockets.events;

import net.minecraftforge.eventbus.api.Event;

public abstract class SocketEvent extends Event {
    public static class Authenticated extends SocketEvent {}

    public static class Disconnected extends SocketEvent {}
}
