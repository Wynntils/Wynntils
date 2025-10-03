/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.event;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.worlds.type.BombInfo;
import net.neoforged.bus.api.Event;

public abstract class BombEvent extends Event {
    private final BombInfo bombInfo;
    private final StyledText message;

    protected BombEvent(BombInfo bombInfo, StyledText message) {
        this.bombInfo = bombInfo;
        this.message = message;
    }

    public static final class Local extends BombEvent {
        public Local(BombInfo bombInfo, StyledText message) {
            super(bombInfo, message);
        }
    }

    public static final class BombBell extends BombEvent {
        public BombBell(BombInfo bombInfo, StyledText message) {
            super(bombInfo, message);
        }
    }

    public BombInfo getBombInfo() {
        return bombInfo;
    }

    public StyledText getMessage() {
        return message;
    }
}
