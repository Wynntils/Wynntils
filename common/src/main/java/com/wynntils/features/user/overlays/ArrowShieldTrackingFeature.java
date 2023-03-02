/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.Category;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.abilities.event.ArrowShieldEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class ArrowShieldTrackingFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final ArrowShieldTrackerOverlay arrowShieldTrackerOverlay = new ArrowShieldTrackerOverlay();

    @SubscribeEvent
    public void onShieldCreated(ArrowShieldEvent.Created event) {
        arrowShieldTrackerOverlay.updateCharges(event.getCharges());
    }

    @SubscribeEvent
    public void onShieldDegraded(ArrowShieldEvent.Degraded event) {
        arrowShieldTrackerOverlay.updateCharges(event.getChargesRemaining());
    }

    @SubscribeEvent
    public void onShieldRemoved(ArrowShieldEvent.Removed event) {
        arrowShieldTrackerOverlay.updateCharges(0);
    }

    public static class ArrowShieldTrackerOverlay extends Overlay {
        private static final String ARROW_SYMBOL = " ⬈"; // leading space is on purpose

        @Config
        public CustomColor textColor = CommonColors.LIGHT_BLUE;

        @Config
        public TextShadow textShadow = TextShadow.OUTLINE;

        private int charges;
        private TextRenderTask renderTaskCache;
        private TextRenderSetting textRenderSetting;

        protected ArrowShieldTrackerOverlay() {
            super(
                    new OverlayPosition(
                            140,
                            -5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.TopRight),
                    new GuiScaledOverlaySize(120, 35));

            updateTextRenderSetting();
        }

        @Override
        public void render(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            if (renderTaskCache == null) return;

            BufferedFontRenderer.getInstance()
                    .renderTextWithAlignment(
                            poseStack,
                            bufferSource,
                            this.getRenderX(),
                            this.getRenderY(),
                            renderTaskCache,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        public void renderPreview(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            BufferedFontRenderer.getInstance()
                    .renderTextWithAlignment(
                            poseStack,
                            bufferSource,
                            this.getRenderX(),
                            this.getRenderY(),
                            new TextRenderTask(getOverlayText(3), textRenderSetting.withCustomColor(textColor)),
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        public void updateCharges(int charges) {
            if (charges == 0) {
                renderTaskCache = null;
                return;
            }

            renderTaskCache = new TextRenderTask(getOverlayText(charges), textRenderSetting.withCustomColor(textColor));
        }

        private static String getOverlayText(int charges) {
            return "Arrow Shield:" + ARROW_SYMBOL.repeat(charges);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            updateTextRenderSetting();
        }

        private void updateTextRenderSetting() {
            textRenderSetting = TextRenderSetting.DEFAULT
                    .withMaxWidth(this.getWidth())
                    .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                    .withTextShadow(textShadow);
        }
    }
}
