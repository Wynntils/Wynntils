/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric;

import com.wynntils.core.WynntilsMod;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;

public class WynntilsModFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WynntilsMod.init(
                new WynntilsMod.Provider() {
                    @Override
                    public void registerStartTickEvent(Consumer<Minecraft> listener) {
                        ClientTickEvents.START_CLIENT_TICK.register(listener::accept);
                    }

                    @Override
                    public void registerEndTickEvent(Consumer<Minecraft> listener) {
                        ClientTickEvents.END_CLIENT_TICK.register(listener::accept);
                    }
                },
                getModVersion());
    }

    public String getModVersion() {
        Optional<ModContainer> wynntilsMod = FabricLoader.getInstance().getModContainer("wynntils");
        if (wynntilsMod.isEmpty()) {
            throw new RuntimeException("Where is my Wynntils?");
        }

        return wynntilsMod.get().getMetadata().getVersion().getFriendlyString();
    }
}
