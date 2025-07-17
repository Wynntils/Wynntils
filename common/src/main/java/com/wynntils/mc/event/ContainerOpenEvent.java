/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/** Fired when a container is opened with its contents */
public class ContainerOpenEvent extends Event {
    private final MenuType<?> menuType;
    private final Component title;
    private final AbstractContainerMenu containerMenu;
    private final List<ItemStack> items;

    public ContainerOpenEvent(
            MenuType<?> menuType, Component title, AbstractContainerMenu containerMenu, List<ItemStack> items) {
        this.menuType = menuType;
        this.title = title;
        this.containerMenu = containerMenu;
        this.items = items;
    }

    public MenuType<?> getMenuType() {
        return menuType;
    }

    public Component getTitle() {
        return title;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public AbstractContainerMenu getContainerMenu() {
        return containerMenu;
    }
}
