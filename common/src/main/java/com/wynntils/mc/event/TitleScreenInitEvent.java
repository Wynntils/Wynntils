/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.eventbus.api.Event;

public class TitleScreenInitEvent extends Event {
    private TitleScreen titleScreen;
    private Consumer<AbstractWidget> addButton;

    public TitleScreenInitEvent() {
    }

    public TitleScreenInitEvent(
        TitleScreen titleScreen,
        Consumer<AbstractWidget> addButton) {
        this.titleScreen = titleScreen;
        this.addButton = addButton;
    }

    public TitleScreen getTitleScreen() {
        return titleScreen;
    }

    public Consumer<AbstractWidget> getAddButton() {
        return addButton;
    }
}
