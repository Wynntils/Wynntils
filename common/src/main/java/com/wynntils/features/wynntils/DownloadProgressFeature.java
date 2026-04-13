/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import static com.wynntils.utils.mc.McUtils.displayToast;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.net.event.DownloadEvent;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class DownloadProgressFeature extends Feature {
    public DownloadProgressFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onDownloadStarted(DownloadEvent.Started event) {
        displayToast(
                Component.translatable("feature.wynntils.downloadProgress.startingDownload"),
                Component.translatable("feature.wynntils.downloadProgress.startingDownloadMessage"),
                10000L);
    }

    @SubscribeEvent
    public void onDownloadCompleted(DownloadEvent.Completed event) {
        displayToast(
                Component.translatable("feature.wynntils.downloadProgress.downloadCompleted"),
                Component.translatable("feature.wynntils.downloadProgress.downloadCompletedMessage"),
                10000L);
    }

    @SubscribeEvent
    public void onDownloadFailed(DownloadEvent.Failed event) {
        displayToast(
                Component.translatable("feature.wynntils.downloadProgress.downloadFailed"),
                Component.translatable("feature.wynntils.downloadProgress.downloadFailedMessage"),
                10000L);
    }
}
