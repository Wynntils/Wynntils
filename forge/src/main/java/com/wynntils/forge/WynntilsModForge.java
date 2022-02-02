/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge;

import com.wynntils.core.WynntilsMod;
import java.util.function.Consumer;

import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(WynntilsMod.MOD_ID)
public class WynntilsModForge {
    public WynntilsModForge() {
        MinecraftForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(
                e -> {
                    //Convert to common form
                    ClientTickEvent.Phase phase = switch (e.phase) {
                        case START -> ClientTickEvent.Phase.START;
                        case END -> ClientTickEvent.Phase.END;
                    };

                    WynntilsMod.getEventBus().post(new ClientTickEvent(phase));
                });

        WynntilsMod.init(getModVersion());
    }

    // Only works on init time
    private String getModVersion() {
        return ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
    }
}
