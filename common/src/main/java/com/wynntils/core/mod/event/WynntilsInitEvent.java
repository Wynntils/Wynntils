/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.mod.event;

import net.neoforged.bus.api.Event;

public abstract class WynntilsInitEvent extends Event {
    /**
     * This event is fired when the mod finishes initializing, and all core components are ready to be used.
     * This event is the last step of the mod initialization process and is called once during the lifetime of the game.
     * Warning: This event being fired does not mean that the mod is fully loaded. Some components, such as features
     *          are loaded only after i18n initialization by Minecraft, as they depend on resources loaded by Minecraft.
     */
    public static class ModInitFinished extends WynntilsInitEvent {}
}
