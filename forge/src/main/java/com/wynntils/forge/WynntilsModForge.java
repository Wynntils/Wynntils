/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge;

import com.wynntils.core.WynntilsMod;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(WynntilsMod.MOD_ID)
public class WynntilsModForge {
    public WynntilsModForge() {
        // if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) return;
        // Stops it from running on integrated server for single player, probably unnecessary for
        // most cases
        WynntilsMod.init(
                new WynntilsMod.Provider() {
                    @Override
                    public void registerStartTickEvent(Consumer<Minecraft> listener) {
                        MinecraftForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(
                                e -> {
                                    if (e.phase == TickEvent.Phase.START) {
                                        listener.accept(Minecraft.getInstance());
                                    }
                                });
                    }

                    @Override
                    public void registerEndTickEvent(Consumer<Minecraft> listener) {
                        MinecraftForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(
                                e -> {
                                    if (e.phase == TickEvent.Phase.END) {
                                        listener.accept(Minecraft.getInstance());
                                    }
                                });
                    }
                },
                getModVersion());
    }

    // Only works on init time
    private String getModVersion() {
        return ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
    }
}
