/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraftforge.eventbus.api.Event;

public class ContainerScreenInitEvent extends Event {
    private final ContainerScreen containerScreen;
    private final Consumer<AbstractWidget> addButton;

    public ContainerScreenInitEvent(ContainerScreen containerScreen, Consumer<AbstractWidget> addButton) {
        this.containerScreen = containerScreen;
        this.addButton = addButton;
    }

    public ContainerScreen getContainerScreen() {
        return containerScreen;
    }

    public Consumer<AbstractWidget> getAddButton() {
        return addButton;
    }
}
