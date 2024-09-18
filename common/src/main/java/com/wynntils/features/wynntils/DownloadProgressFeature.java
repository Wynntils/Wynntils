/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class DownloadProgressFeature extends Feature {
    // FIXME: Needs events
    @SubscribeEvent
    public void onDownloadStarted() {
        displayToast(
                Component.translatable("feature.wynntils.downloadProgress.startingDownload"),
                Component.literal("Download name"));
    }

    @SubscribeEvent
    public void onDownloadCompleted() {
        displayToast(
                Component.translatable("feature.wynntils.downloadProgress.downloadCompleted"),
                Component.literal("Download name"));
    }

    @SubscribeEvent
    public void onDownloadFailed() {
        displayToast(
                Component.translatable("feature.wynntils.downloadProgress.downloadFailed"),
                Component.literal("Download name"));
    }

    private void displayToast(Component title, Component message) {
        McUtils.mc().getToasts().addToast(new SystemToast(new SystemToast.SystemToastId(10000L), title, message));
    }
}
