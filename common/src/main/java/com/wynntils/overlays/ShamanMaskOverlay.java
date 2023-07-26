/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.models.abilities.event.ShamanMaskTitlePacketEvent;
import com.wynntils.models.abilities.type.ShamanMaskType;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShamanMaskOverlay extends TextOverlay {
    private static final String TEMPLATE = "{shaman_mask} mask";

    @RegisterConfig
    public final Config<Boolean> hideMaskTitles = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> displayNone = new Config<>(false);

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
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        ShamanMaskType currentMaskType = Models.ShamanMask.getCurrentMaskType();

        if (currentMaskType == ShamanMaskType.NONE && !displayNone.get()) return;

        super.render(poseStack, bufferSource, partialTicks, window);
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
