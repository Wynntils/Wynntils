/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.framework.events.Event;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;

public class GameMenuInitEvent extends Event {
    private final PauseScreen pauseScreen;
    private final List<AbstractWidget> buttons;

    public GameMenuInitEvent(PauseScreen pauseScreen, List<AbstractWidget> buttons) {
        this.pauseScreen = pauseScreen;
        this.buttons = buttons;
    }

    public PauseScreen getPauseScreen() {
        return pauseScreen;
    }

    public List<AbstractWidget> getButtons() {
        return buttons;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
