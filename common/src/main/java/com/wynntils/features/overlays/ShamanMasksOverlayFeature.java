/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.Overlay;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.TextOverlay;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.abilities.event.ShamanMaskTitlePacketEvent;
import com.wynntils.models.abilities.type.ShamanMaskType;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class ShamanMasksOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final Overlay shamanMaskOverlay = new ShamanMaskOverlay();

    @RegisterConfig
    public final Config<Boolean> hideMaskTitles = new Config<>(true);

    @SubscribeEvent
    public void onShamanMaskTitle(ShamanMaskTitlePacketEvent event) {
        if (hideMaskTitles.get()) {
            event.setCanceled(true);
        }
    }

    public static class ShamanMaskOverlay extends TextOverlay {
        private static final String TEMPLATE = "{shaman_mask} mask";

        @RegisterConfig
        public final Config<Boolean> displayNone = new Config<>(false);

        protected ShamanMaskOverlay() {
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
}
