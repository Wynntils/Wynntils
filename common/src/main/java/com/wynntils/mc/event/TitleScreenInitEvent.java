/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.framework.events.Event;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;

public class TitleScreenInitEvent extends Event {
    private final TitleScreen titleScreen;
    private final List<AbstractWidget> buttons;
    private final Consumer<AbstractWidget> addButton;

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

    @Override
    public boolean isCancellable() {
        return false;
    }
}
