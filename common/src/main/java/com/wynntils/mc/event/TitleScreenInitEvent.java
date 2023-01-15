/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;

/** Fired on initialization of {@link TitleScreen} */
public abstract class TitleScreenInitEvent extends WynntilsEvent {
    private final TitleScreen titleScreen;
    private final Consumer<AbstractWidget> addButton;

    protected TitleScreenInitEvent(TitleScreen titleScreen, Consumer<AbstractWidget> addButton) {
        this.titleScreen = titleScreen;
        this.addButton = addButton;
    }

    public TitleScreen getTitleScreen() {
        return titleScreen;
    }

    public Consumer<AbstractWidget> getAddButton() {
        return addButton;
    }

    public static class Pre extends TitleScreenInitEvent {
        public Pre(TitleScreen titleScreen, Consumer<AbstractWidget> addButton) {
            super(titleScreen, addButton);
        }
    }

    public static class Post extends TitleScreenInitEvent {
        public Post(TitleScreen titleScreen, Consumer<AbstractWidget> addButton) {
            super(titleScreen, addButton);
        }
    }
}
