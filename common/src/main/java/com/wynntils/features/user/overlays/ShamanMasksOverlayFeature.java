/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.render.buffered.BufferedFontRenderer;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.models.abilities.event.ShamanMaskTitlePacketEvent;
import com.wynntils.models.abilities.type.ShamanMaskType;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShamanMasksOverlayFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final Overlay shamanMaskOverlay = new ShamanMaskOverlay();

    @Config
    public boolean hideMaskTitles = true;

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.ShamanMask);
    }

    @SubscribeEvent
    public void onShamanMaskTitle(ShamanMaskTitlePacketEvent event) {
        if (hideMaskTitles && shamanMaskOverlay.isEnabled()) {
            event.setCanceled(true);
        }
    }

    public static class ShamanMaskOverlay extends Overlay {
        @Config
        public String maskDisplay = "%mask% mask";

        @Config
        public boolean displayNone = false;

        protected ShamanMaskOverlay() {
            super(
                    new OverlayPosition(
                            -60,
                            150,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21),
                    HorizontalAlignment.Center,
                    VerticalAlignment.Middle);
        }

        @Override
        public void render(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            ShamanMaskType currentMaskType = Models.ShamanMask.getCurrentMaskType();

            if (currentMaskType == ShamanMaskType.NONE && !displayNone) return;

            renderMaskString(poseStack, bufferSource, currentMaskType);
        }

        @Override
        public void renderPreview(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            renderMaskString(poseStack, bufferSource, ShamanMaskType.AWAKENED);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}

        private void renderMaskString(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, ShamanMaskType currentMaskType) {
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            maskDisplay.replace("%mask%", currentMaskType.getName()),
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            this.getRenderY(),
                            this.getRenderY() + this.getHeight(),
                            0,
                            CustomColor.fromChatFormatting(currentMaskType.getColor()),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment(),
                            TextShadow.OUTLINE);
        }
    }
}
