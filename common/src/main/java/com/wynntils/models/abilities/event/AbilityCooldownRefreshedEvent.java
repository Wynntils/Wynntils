/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.event;

import com.wynntils.core.text.StyledText;
import net.neoforged.bus.api.Event;

public class AbilityCooldownRefreshedEvent extends Event {
    private final StyledText message;
    private boolean cancelMessage = false;

    public AbilityCooldownRefreshedEvent(StyledText message) {
        this.message = message;
    }

    public void setCancelMessage(boolean cancelMessage) {
        this.cancelMessage = cancelMessage;
    }

    public boolean shouldCancelMessage() {
        return cancelMessage;
    }

    public StyledText getMessage() {
        return message;
    }
}
