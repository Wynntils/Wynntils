/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.quilt;

import com.wynntils.core.WynntilsMod;
import java.io.File;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class WynntilsModQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        WynntilsMod.init(
                WynntilsMod.ModLoader.QUILT,
                mod.metadata().version().raw(),
                QuiltLoader.isDevelopmentEnvironment(),
                new File(mod.getSourcePaths().get(0).get(0).toUri()));
    }
}
