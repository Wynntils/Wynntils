/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.wynn.event.StatusEffectsChangedEvent;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class StatusOverlayFeature extends UserFeature {
    private List<TextRenderTask> renderCache = List.of();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final StatusOverlay statusOverlay = new StatusOverlay();

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.Tab);
    }

    @SubscribeEvent
    public void onStatusChange(StatusEffectsChangedEvent event) {
        recalculateRenderCache();
    }

    private void recalculateRenderCache() {
        renderCache = Models.Tab.getTimers().stream()
                .map(statusTimer -> new TextRenderTask(statusTimer.asString(), statusOverlay.getTextRenderSetting()))
                .toList();
    }

    public class StatusOverlay extends Overlay {
        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        private TextRenderSetting textRenderSetting;

        protected StatusOverlay() {
            super(
                    new OverlayPosition(
                            55,
                            -5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.TopRight),
                    new GuiScaledOverlaySize(250, 110));

            updateTextRenderSetting();
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            StatusOverlayFeature.this.renderCache,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            FontRenderer.getInstance()
                    .renderTextWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            new TextRenderTask("§8⬤ §7 Purification 00:02", textRenderSetting),
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            updateTextRenderSetting();
            StatusOverlayFeature.this.recalculateRenderCache();
        }

        private void updateTextRenderSetting() {
            textRenderSetting = TextRenderSetting.DEFAULT
                    .withMaxWidth(this.getWidth())
                    .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                    .withTextShadow(textShadow);
        }

        public TextRenderSetting getTextRenderSetting() {
            return textRenderSetting;
        }
    }
}
