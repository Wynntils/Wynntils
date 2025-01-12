/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.neoforge;

import com.wynntils.core.WynntilsMod;
import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import java.io.File;
import java.nio.file.Path;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(WynntilsMod.MOD_ID)
public class WynntilsModForge {
    public WynntilsModForge() {
        Path path = ModLoadingContext.get()
                .getActiveContainer()
                .getModInfo()
                .getOwningFile()
                .getFile()
                .getFilePath();

        File modFile = new File(path.toUri());

        WynntilsMod.init(
                WynntilsMod.ModLoader.FORGE,
                ModLoadingContext.get()
                        .getActiveContainer()
                        .getModInfo()
                        .getVersion()
                        .toString(),
                !FMLEnvironment.production,
                modFile);

        ModLoadingContext.get()
                .registerExtensionPoint(
                        IConfigScreenFactory.class, () -> (mc, parent) -> WynntilsBookSettingsScreen.create(parent));
    }
}
