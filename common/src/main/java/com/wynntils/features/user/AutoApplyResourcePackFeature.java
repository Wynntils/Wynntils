/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Managers;
import com.wynntils.mc.event.ResourcePackClearEvent;
import com.wynntils.mc.event.ResourcePackEvent;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.mc.utils.McUtils;
import java.io.File;
import java.util.Objects;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AutoApplyResourcePackFeature extends UserFeature {
    private final File SERVER_RESOURCE_PACK_DIR = new File(McUtils.mc().gameDirectory, "server-resource-packs");

    @Config(visible = false)
    private String packHash = "";

    private String appliedHash = "";

    @SubscribeEvent
    public void onResourcePackLoad(ResourcePackEvent event) {
        if (!Managers.WORLD_STATE.onServer()) return;

        if (Objects.equals(event.getHash(), appliedHash)) {
            event.setCanceled(true);
            return;
        }

        packHash = event.getHash();
        Managers.CONFIG.saveConfig();
    }

    @SubscribeEvent
    public void onResourceClear(ResourcePackClearEvent event) {
        if (appliedHash.isEmpty()) return;

        if (Objects.equals(event.getHash(), appliedHash)) {
            event.setCanceled(true);
            return;
        }

        appliedHash = "";
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTitleScreenInit(TitleScreenInitEvent.Pre event) {
        if (packHash == null || packHash.isEmpty() || Objects.equals(appliedHash, packHash)) return;

        ClientPackSource clientPackSource = McUtils.mc().getClientPackSource();

        File[] files = SERVER_RESOURCE_PACK_DIR.listFiles();

        for (File file : files != null ? files : new File[0]) {
            if (clientPackSource.checkHash(packHash, file)) {
                clientPackSource.setServerPack(file, PackSource.DEFAULT);
                appliedHash = packHash;
                break;
            }
        }
    }
}
