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
    private List<AbstractWidget> buttons;
    private Consumer<AbstractWidget> addButton;

    public TitleScreenInitEvent() {
    }

    public TitleScreenInitEvent(
            TitleScreen titleScreen,
            List<AbstractWidget> buttons,
            Consumer<AbstractWidget> addButton) {
        this.titleScreen = titleScreen;
        this.buttons = buttons;
        this.addButton = addButton;
    }

    public TitleScreen getTitleScreen() {
        return titleScreen;
    }

    public List<AbstractWidget> getButtons() {
        return buttons;
    }

    public Consumer<AbstractWidget> getAddButton() {
        return addButton;
    }
}
