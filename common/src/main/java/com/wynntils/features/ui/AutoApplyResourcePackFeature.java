/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.storage.Storage;
import com.wynntils.mc.event.ResourcePackClearEvent;
import com.wynntils.mc.event.ResourcePackEvent;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.client.resources.DownloadedPackSource;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class AutoApplyResourcePackFeature extends UserFeature {
    private static final File SERVER_RESOURCE_PACK_DIR = new File(McUtils.mc().gameDirectory, "server-resource-packs");

    private Storage<String> packHash = new Storage<>("");

    private String appliedHash = "";

    @SubscribeEvent
    public void onResourcePackLoad(ResourcePackEvent event) {
        if (Objects.equals(event.getHash(), appliedHash)) {
            event.setCanceled(true);
            return;
        }

        packHash.store(event.getHash());
        Managers.Config.saveConfig();
    }

    @SubscribeEvent
    public void onResourceClear(ResourcePackClearEvent event) {
        if (appliedHash.isEmpty()) return;

        try (PackResources packResources = event.getServerPack().open()) {
            if (packResources instanceof FilePackResources filePackResources) {
                String hash = Files.asByteSource(filePackResources.file)
                        .hash(Hashing.sha1())
                        .toString();

                if (Objects.equals(hash, appliedHash)) {
                    event.setCanceled(true);
                    return;
                }
            }
        } catch (IOException e) {
            // ignored
        }

        appliedHash = "";
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTitleScreenInit(TitleScreenInitEvent.Pre event) {
        if (packHash.get().isEmpty() || Objects.equals(appliedHash, packHash.get())) return;

        DownloadedPackSource downloadedPackSource = McUtils.mc().getDownloadedPackSource();

        File[] files = SERVER_RESOURCE_PACK_DIR.listFiles();

        for (File file : files != null ? files : new File[0]) {
            if (downloadedPackSource.checkHash(packHash.get(), file)) {
                downloadedPackSource.setServerPack(file, PackSource.DEFAULT);
                appliedHash = packHash.get();
                break;
            }
        }
    }
}
