package com.wynntils.mc.event;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Consumer;

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
