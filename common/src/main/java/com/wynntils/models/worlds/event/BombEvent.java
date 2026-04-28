/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.event;

import com.wynntils.models.worlds.type.BombInfo;
import net.neoforged.bus.api.Event;

public abstract class BombEvent extends Event {
    private final BombInfo bombInfo;

    protected BombEvent(BombInfo bombInfo) {
        this.bombInfo = bombInfo;
    }

    public static final class Local extends BombEvent {
        public Local(BombInfo bombInfo) {
            super(bombInfo);
        }
    }

    public static final class BombBell extends BombEvent {
        public BombBell(BombInfo bombInfo) {
            super(bombInfo);
        }
    }

    public BombInfo getBombInfo() {
        return bombInfo;
    }
}
