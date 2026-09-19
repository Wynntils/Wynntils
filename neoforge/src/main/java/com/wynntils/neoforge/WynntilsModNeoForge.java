/*
 * Copyright © Wynntils 2021-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.neoforge;

import com.wynntils.core.WynntilsMod;
import com.wynntils.screens.settings.WynntilsFeaturesSettingsScreen;
import com.wynntils.utils.BugReportUtils;
import java.io.File;
import java.nio.file.Path;
import java.util.stream.Collectors;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(WynntilsMod.MOD_ID)
public class WynntilsModNeoForge {
    public WynntilsModNeoForge() {
        BugReportUtils.registerPlatform(
                () -> ModList.get().getMods().stream()
                        .map(mod -> mod.getModId() + " " + mod.getVersion())
                        .sorted()
                        .collect(Collectors.joining("\n")),
                () -> ModList.get().getMods().stream()
                        .filter(mod -> mod.getModId().equals("neoforge"))
                        .map(mod -> mod.getVersion().toString())
                        .findFirst()
                        .orElse("Unknown"));
        Path path = ModLoadingContext.get()
                .getActiveContainer()
                .getModInfo()
                .getOwningFile()
                .getFile()
                .getFilePath();

        File modFile = new File(path.toUri());

        WynntilsMod.init(
                WynntilsMod.ModLoader.NEOFORGE,
                ModLoadingContext.get()
                        .getActiveContainer()
                        .getModInfo()
                        .getVersion()
                        .toString(),
                !FMLEnvironment.isProduction(),
                modFile);

        ModLoadingContext.get()
                .registerExtensionPoint(
                        IConfigScreenFactory.class,
                        () -> (mc, parent) -> WynntilsFeaturesSettingsScreen.create(parent));
    }
}
