/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.eventbus.api.Event;

/** Fired on initialization of {@link PauseScreen} */
public final class PauseMenuInitEvent extends Event {
    private final PauseScreen pauseScreen;

    public PauseMenuInitEvent(PauseScreen pauseScreen) {
        this.pauseScreen = pauseScreen;
    }

    public PauseScreen getPauseScreen() {
        return pauseScreen;
    }
}
