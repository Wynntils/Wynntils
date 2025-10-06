/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hades.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.EventThread;

public abstract class HadesEvent extends BaseEvent {
    @EventThread(EventThread.Type.IO)
    public static final class Authenticated extends HadesEvent {}

    public static final class Disconnected extends HadesEvent {}
}
