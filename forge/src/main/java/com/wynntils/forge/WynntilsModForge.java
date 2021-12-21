/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge;

import com.wynntils.forge.listener.ScreenListener;
import me.shedaniel.architectury.platform.forge.EventBuses;
import com.wynntils.WynntilsMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WynntilsMod.MOD_ID)
public class WynntilsModForge {
    public WynntilsModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(WynntilsMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        WynntilsMod.init();
        MinecraftForge.EVENT_BUS.register(new ScreenListener());
    }
}
