/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;

/** Fired for Menu events */
public abstract class MenuEvent extends BaseEvent {
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

        public static final class Pre extends MenuOpenedEvent implements CancelRequestable {
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
    public static final class MenuClosedEvent extends MenuEvent implements CancelRequestable {
        private final int containerId;

        public MenuClosedEvent(int containerId) {
            this.containerId = containerId;
        }

        public int getContainerId() {
            return containerId;
        }
    }
}
