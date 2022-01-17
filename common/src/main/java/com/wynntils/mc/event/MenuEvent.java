/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

/** Fired for Menu events */
public abstract class MenuEvent extends Event {
    /** Fired for Menu opened events */
    public static class MenuOpenedEvent extends MenuEvent {
        public static final ResourceLocation MENU_3_LINES =
                new ResourceLocation("minecraft:generic_9x3");

        private final ResourceLocation menuType;
        private final Component title;

        public MenuOpenedEvent(ResourceLocation menuType, Component title) {
            this.menuType = menuType;
            this.title = title;
        }

        public ResourceLocation getMenuType() {
            return menuType;
        }

        public Component getTitle() {
            return title;
        }
    }

    /** Fired for Menu closed events */
    public static class MenuClosedEvent extends MenuEvent {}
}
