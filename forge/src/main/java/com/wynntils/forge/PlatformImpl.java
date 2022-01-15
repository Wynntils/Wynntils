/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge;

import net.minecraftforge.fml.ModLoadingContext;

public class PlatformImpl {
    public static String getModVersion() {
        return ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
    }
}
