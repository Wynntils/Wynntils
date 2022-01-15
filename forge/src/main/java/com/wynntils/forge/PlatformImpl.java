package com.wynntils.forge;

import net.minecraftforge.fml.ModLoadingContext;

public class PlatformImpl {
    public static String getModVersion() {
        return ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
    }
}
