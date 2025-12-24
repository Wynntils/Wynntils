/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.net.event.DownloadEvent;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class DownloadProgressFeature extends Feature {
    public DownloadProgressFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }

    @SubscribeEvent
    public void onDownloadStarted(DownloadEvent.Started event) {
        displayToast(
                Component.translatable("feature.wynntils.downloadProgress.startingDownload"),
                Component.translatable("feature.wynntils.downloadProgress.startingDownloadMessage"));
    }

    @SubscribeEvent
    public void onDownloadCompleted(DownloadEvent.Completed event) {
        displayToast(
                Component.translatable("feature.wynntils.downloadProgress.downloadCompleted"),
                Component.translatable("feature.wynntils.downloadProgress.downloadCompletedMessage"));
    }

    @SubscribeEvent
    public void onDownloadFailed(DownloadEvent.Failed event) {
        displayToast(
                Component.translatable("feature.wynntils.downloadProgress.downloadFailed"),
                Component.translatable("feature.wynntils.downloadProgress.downloadFailedMessage"));
    }

    private void displayToast(Component title, Component message) {
        McUtils.mc().getToastManager().addToast(new SystemToast(new SystemToast.SystemToastId(10000L), title, message));
    }
}
