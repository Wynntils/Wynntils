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
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.Overlay;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.statuseffects.event.StatusEffectsChangedEvent;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class StatusOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final StatusOverlay statusOverlay = new StatusOverlay();

    public class StatusOverlay extends Overlay {
        @RegisterConfig
        public final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

        @RegisterConfig
        public final Config<Float> fontScale = new Config<>(1.0f);

        private List<TextRenderTask> renderCache = List.of();
        private TextRenderSetting textRenderSetting;

        protected StatusOverlay() {
            super(
                    new OverlayPosition(
                            55,
                            -5,
                            VerticalAlignment.TOP,
                            HorizontalAlignment.RIGHT,
                            OverlayPosition.AnchorSection.TOP_RIGHT),
                    new OverlaySize(250, 110));

            updateTextRenderSetting();
        }

        @SubscribeEvent
        public void onStatusChange(StatusEffectsChangedEvent event) {
            recalculateRenderCache();
        }

        private void recalculateRenderCache() {
            renderCache = Models.StatusEffect.getStatusEffects().stream()
                    .map(statusTimer ->
                            new TextRenderTask(statusTimer.asString(), statusOverlay.getTextRenderSetting()))
                    .toList();
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
            BufferedFontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            bufferSource,
                            this.getRenderX(),
                            this.getRenderY(),
                            renderCache,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment(),
                            fontScale.get());
        }

        @Override
        public void renderPreview(
                PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
            BufferedFontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            bufferSource,
                            this.getRenderX(),
                            this.getRenderY(),
                            List.of(new TextRenderTask(
                                    StyledText.fromString("§8⬤ §7 Purification 00:02"), textRenderSetting)),
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment(),
                            fontScale.get());
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            updateTextRenderSetting();
            recalculateRenderCache();
        }

        private void updateTextRenderSetting() {
            textRenderSetting = TextRenderSetting.DEFAULT
                    .withMaxWidth(this.getWidth())
                    .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                    .withTextShadow(textShadow.get());
        }

        protected TextRenderSetting getTextRenderSetting() {
            return textRenderSetting;
        }
    }
}
