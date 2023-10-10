/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Fired for Menu events */
public abstract class MenuEvent extends Event {
    /** Fired for Menu opened events */
    public abstract static class MenuOpenedEvent extends MenuEvent {
        private final MenuType<?> menuType;
        private final Component title;
        private final int containerId;

        protected MenuOpenedEvent(MenuType<?> menuType, Component title, int containerId) {
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

        @Cancelable
        public static final class Pre extends MenuOpenedEvent {
            public Pre(MenuType<?> menuType, Component title, int containerId) {
                super(menuType, title, containerId);
            }
        }

        public static final class Post extends MenuOpenedEvent {
            public Post(MenuType<?> menuType, Component title, int containerId) {
                super(menuType, title, containerId);
            }
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
