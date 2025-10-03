/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.client.gui.screens.PauseScreen;

/** Fired on initialization of {@link PauseScreen} */
public final class PauseMenuInitEvent extends BaseEvent {
    private final PauseScreen pauseScreen;

    public PauseMenuInitEvent(PauseScreen pauseScreen) {
        this.pauseScreen = pauseScreen;
    }

    public PauseScreen getPauseScreen() {
        return pauseScreen;
    }
}
