/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class MobTotemTrackingFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final MobTotemTimerOverlay mobTotemTimerOverlay = new MobTotemTimerOverlay();

    @SubscribeEvent
    public void onTick(TickEvent event) {
        mobTotemTimerOverlay.ticksUntilUpdate--;

        if (mobTotemTimerOverlay.ticksUntilUpdate <= 0) {
            mobTotemTimerOverlay.ticksUntilUpdate = MobTotemTimerOverlay.TICKS_PER_UPDATE;
            mobTotemTimerOverlay.updateRenderTaskCache();
        }
    }

    public static class MobTotemTimerOverlay extends Overlay {
        static final int TICKS_PER_UPDATE = 5;

        @Config
        public TextShadow textShadow = TextShadow.OUTLINE;

        private TextRenderSetting textRenderSetting;

        private int ticksUntilUpdate = 0;
        private List<TextRenderTask> renderTaskCache;

        protected MobTotemTimerOverlay() {
            super(
                    new OverlayPosition(
                            330,
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
            if (renderTaskCache == null) {
                updateRenderTaskCache();
            }

            BufferedFontRenderer.getInstance()
                    .renderTextsWithAlignment(
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
                            new TextRenderTask("Mob Totem (Player) at [-105, 58, 3948] (4:11)", textRenderSetting),
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        void updateRenderTaskCache() {
            renderTaskCache = Models.MobTotem.getMobTotems().stream()
                    .map(mobTotem -> {
                        // find direction from where the player is looking to mob totem
                        double angleDiff = mobTotem.getLookAngleDiff();
                        String direction;
                        if (angleDiff > 337.5 || angleDiff < 22.5) {
                            direction = "⬆";
                        } else if (angleDiff > 22.5 && angleDiff < 67.5) {
                            direction = "⬉";
                        } else if (angleDiff > 67.5 && angleDiff < 112.5) {
                            direction = "⬅";
                        } else if (angleDiff > 112.5 && angleDiff < 157.5) {
                            direction = "⬋";
                        } else if (angleDiff > 157.5 && angleDiff < 202.5) {
                            direction = "⬇";
                        } else if (angleDiff > 202.5 && angleDiff < 247.5) {
                            direction = "⬊";
                        } else if (angleDiff > 247.5 && angleDiff < 292.5) {
                            direction = "➡";
                        } else if (angleDiff > 292.5 && angleDiff < 337.5) {
                            direction = "⬈";
                        } else {
                            direction = "?";
                        }

                        return new TextRenderTask(
                                "Mob Totem (" + mobTotem.getOwner() + ") at " + direction + " ("
                                        + mobTotem.getTimerString() + ")",
                                textRenderSetting);
                    })
                    .toList();
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
