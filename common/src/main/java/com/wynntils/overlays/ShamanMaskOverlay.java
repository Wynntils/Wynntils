/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.models.abilities.event.ShamanMaskTitlePacketEvent;
import com.wynntils.models.abilities.type.ShamanMaskType;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.bus.api.SubscribeEvent;

public class ShamanMaskOverlay extends TextOverlay {
    private static final String TEMPLATE = "{shaman_mask} mask";

    @Persisted
    private final Config<Boolean> hideMaskTitles = new Config<>(true);

    @Persisted
    private final Config<Boolean> displayNone = new Config<>(false);

    public ShamanMaskOverlay() {
        super(
                new OverlayPosition(
                        -60,
                        150,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(81, 21),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE);
    }

    @SubscribeEvent
    public void onShamanMaskTitle(ShamanMaskTitlePacketEvent event) {
        if (hideMaskTitles.get()) {
            event.setCanceled(true);
        }
    }

    @Override
    public void render(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        ShamanMaskType currentMaskType = Models.ShamanMask.getCurrentMaskType();

        if (currentMaskType == ShamanMaskType.NONE && !displayNone.get()) return;

        super.render(guiGraphics, bufferSource, deltaTracker, window);
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public String getPreviewTemplate() {
        return ShamanMaskType.AWAKENED.getName() + " mask";
    }
}
