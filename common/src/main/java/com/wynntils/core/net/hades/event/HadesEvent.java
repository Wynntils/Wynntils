/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.hades.event;

import com.wynntils.core.events.WynntilsEvent;

public abstract class HadesEvent extends WynntilsEvent {
    public static class Authenticated extends HadesEvent {}

    public static class Disconnected extends HadesEvent {}
}
