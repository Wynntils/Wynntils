/*
 * Copyright © Wynntils 2022.
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
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.render.buffered.BufferedFontRenderer;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.MathUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class AuraTimerOverlayFeature extends UserFeature {
    private static final float MAX_INTENSITY = 0.4f;
    private static final int AURA_PROC_MS = 3200;
    private static final String AURA_TITLE = "§4§n/!\\§7 Tower §6Aura";

    @Config
    public boolean vignetteOnAura = true;

    @Config
    public CustomColor vignetteColor = CommonColors.ORANGE;

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay auraTimerOverlay = new AuraTimerOverlay();

    private long lastAuraProc = 0;

    @SubscribeEvent
    public void onSubtitle(SubtitleSetTextEvent event) {
        if (!event.getComponent().getString().equals(AURA_TITLE)) return;

        lastAuraProc = System.currentTimeMillis();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGui(RenderEvent.Post event) {
        if (!vignetteOnAura || event.getType() != RenderEvent.ElementType.GUI) return;
        long remainingTimeUntilAura = getRemainingTimeUntilAura();
        if (remainingTimeUntilAura <= 0) return;

        RenderUtils.renderVignetteOverlay(
                event.getPoseStack(),
                vignetteColor,
                MathUtils.map(remainingTimeUntilAura, AURA_PROC_MS, 0, 0, MAX_INTENSITY));
    }

    private long getRemainingTimeUntilAura() {
        return AURA_PROC_MS - (System.currentTimeMillis() - lastAuraProc);
    }

    public class AuraTimerOverlay extends Overlay {
        @Config
        public TextShadow textShadow = TextShadow.OUTLINE;

        @Config
        public CustomColor textColor = CommonColors.ORANGE;

        protected AuraTimerOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.Middle),
                    new GuiScaledOverlaySize(150, 30),
                    HorizontalAlignment.Center,
                    VerticalAlignment.Middle);
        }

        @Override
        public void render(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            long remainingMs = getRemainingTimeUntilAura();
            if (remainingMs <= 0) return;

            String renderedString = "Aura: %.1fs".formatted(remainingMs / 1000f);

            renderText(poseStack, bufferSource, renderedString);
        }

        @Override
        public void renderPreview(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            renderText(poseStack, bufferSource, "Aura: 3.2s");
        }

        private void renderText(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, String renderedString) {
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            renderedString,
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            this.getRenderY(),
                            this.getRenderY() + this.getHeight(),
                            0,
                            textColor,
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment(),
                            textShadow,
                            this.getHeight() / 15f);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }
}
