/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.Cancelable;

/** Fired for Menu events */
public abstract class MenuEvent extends WynntilsEvent {
    /** Fired for Menu opened events */
    @Cancelable
    public static class MenuOpenedEvent extends MenuEvent {
        private final MenuType<?> menuType;
        private final Component title;
        private final int containerId;

        public MenuOpenedEvent(MenuType<?> menuType, Component title, int containerId) {
            this.menuType = menuType;
            this.title = title;
            this.containerId = containerId;
        }

        public MenuType<?> getMenuType() {
            return menuType;
        }

        public Component getTitle() {
            return title;
        }

        public int getContainerId() {
            return containerId;
        }
    }

    /** Fired for Menu closed events */
    @Cancelable
    public static class MenuClosedEvent extends MenuEvent {
        private final int containerId;

        public MenuClosedEvent(int containerId) {
            this.containerId = containerId;
        }

        public int getContainerId() {
            return containerId;
        }
    }
}
