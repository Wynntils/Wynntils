/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.TitleScreenInitEvent;
import java.io.File;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

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

        WynntilsMod.registerEventListener(this);
    }

    // This is slightly hacky to do this, but it works
    @SubscribeEvent
    public void onClientLoad(TitleScreenInitEvent.Pre event) {
        // Enable stencil support
        Minecraft.getInstance().getMainRenderTarget().enableStencil();

        WynntilsMod.unregisterEventListener(this);
    }
}
