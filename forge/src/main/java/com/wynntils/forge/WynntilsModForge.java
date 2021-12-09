package com.wynntils.forge;

import me.shedaniel.architectury.platform.forge.EventBuses;
import com.wynntils.WynntilsMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WynntilsMod.MOD_ID)
public class WynntilsModForge {
    public WynntilsModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(WynntilsMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        WynntilsMod.init();
    }
}
