/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.core.Position;

public class CharacterMovedEvent extends BaseEvent {
    private final Position position;

    public CharacterMovedEvent(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}
