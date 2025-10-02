/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.text.StyledText;

/** Fires on change to footer of scoreboard */
public final class PlayerInfoFooterChangedEvent extends BaseEvent {
    private final StyledText footer;

    public StyledText getFooter() {
        return footer;
    }

    public PlayerInfoFooterChangedEvent(StyledText footer) {
        this.footer = footer;
    }
}
