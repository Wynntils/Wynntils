/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Consumer;

/** Fired on initalization of {@link PauseScreen} */
public class GameMenuInitEvent extends Event {
    private final PauseScreen pauseScreen;
    private final Consumer<AbstractWidget> addButton;

    public GameMenuInitEvent(PauseScreen pauseScreen, Consumer<AbstractWidget> addButton) {
        this.pauseScreen = pauseScreen;
        this.addButton = addButton;
    }

    public PauseScreen getPauseScreen() {
        return pauseScreen;
    }

    public Consumer<AbstractWidget> getAddButton() {
        return addButton;
    }
}
