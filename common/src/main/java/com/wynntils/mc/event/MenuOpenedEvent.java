/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.Event;

public class MenuOpenedEvent extends Event {
    private final MenuType<?> menuType;
    private final Component title;

    public MenuOpenedEvent(MenuType<?> menuType, Component title) {
        this.menuType = menuType;
        this.title = title;
    }

    public MenuType<?> getMenuType() {
        return menuType;
    }

    public Component getTitle() {
        return title;
    }
}
