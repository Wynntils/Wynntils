/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.abilities.event.TotemEvent;
import com.wynntils.models.abilities.type.ShamanTotem;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class ShamanTotemTrackingFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final ShamanTotemTimerOverlay shamanTotemTimerOverlay = new ShamanTotemTimerOverlay();

    @Config
    public boolean highlightShamanTotems = true;

    @Config
    public static CustomColor firstTotemColor = CommonColors.WHITE;

    @Config
    public static CustomColor secondTotemColor = CommonColors.BLUE;

    @Config
    public static CustomColor thirdTotemColor = CommonColors.RED;

    private static final int ENTITY_GLOWING_FLAG = 6;

    @SubscribeEvent
    public void onTotemSummoned(TotemEvent.Summoned e) {
        if (!highlightShamanTotems) return;

        int totemNumber = e.getTotemNumber();
        ArmorStand totemAS = e.getTotemEntity();

        CustomColor color =
                switch (totemNumber) {
                    case 1 -> firstTotemColor;
                    case 2 -> secondTotemColor;
                    case 3 -> thirdTotemColor;
                    default -> throw new IllegalArgumentException(
                            "totemNumber should be 1, 2, or 3! (color switch in #onTotemSummoned in ShamanTotemTrackingFeature");
                };

        ((EntityExtension) totemAS).setGlowColor(color);

        totemAS.setGlowingTag(true);
        totemAS.setSharedFlag(ENTITY_GLOWING_FLAG, true);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        shamanTotemTimerOverlay.ticksUntilUpdate--;

        if (shamanTotemTimerOverlay.ticksUntilUpdate <= 0) {
            shamanTotemTimerOverlay.ticksUntilUpdate = ShamanTotemTimerOverlay.TICKS_PER_UPDATE;
            shamanTotemTimerOverlay.updateRenderTaskCache();
        }
    }

    public static class ShamanTotemTimerOverlay extends Overlay {
        static final int TICKS_PER_UPDATE = 5;

        @Config
        public static TotemTrackingDetail totemTrackingDetail = TotemTrackingDetail.COORDS;

        @Config
        public TextShadow textShadow = TextShadow.OUTLINE;

        private TextRenderSetting textRenderSetting;

        private int ticksUntilUpdate = 0;
        private List<TextRenderTask> renderTaskCache;

        protected ShamanTotemTimerOverlay() {
            super(
                    new OverlayPosition(
                            275,
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
                    .renderTextsWithAlignment(
                            poseStack,
                            bufferSource,
                            this.getRenderX(),
                            this.getRenderY(),
                            List.of(
                                    new TextRenderTask(
                                            getFormattedTotemText("Totem 1", " Summoned", ""),
                                            textRenderSetting.withCustomColor(firstTotemColor)),
                                    new TextRenderTask(
                                            getFormattedTotemText("Totem 2", " (01s)", " [-1434, 104, -5823]"),
                                            textRenderSetting.withCustomColor(secondTotemColor)),
                                    new TextRenderTask(
                                            getFormattedTotemText("Totem 3", " (14s)", " [1, 8, -41]"),
                                            textRenderSetting.withCustomColor(thirdTotemColor))),
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        void updateRenderTaskCache() {
            renderTaskCache = Models.ShamanTotem.getActiveTotems().stream()
                    .map(shamanTotem -> {
                        CustomColor color =
                                switch (shamanTotem.getTotemNumber()) {
                                    case 1 -> firstTotemColor;
                                    case 2 -> secondTotemColor;
                                    case 3 -> thirdTotemColor;
                                    default -> throw new IllegalArgumentException(
                                            "totemNumber should be 1, 2, or 3! (switch in #render in ShamanTotemTrackingFeature");
                                };

                        String prefix = "Totem " + shamanTotem.getTotemNumber();

                        String suffix = "";
                        String detail = "";
                        // Check if we should be saying "Summoned"
                        if (shamanTotem.getState() == ShamanTotem.TotemState.SUMMONED) {
                            suffix = " Summoned";
                        } else {
                            switch (totemTrackingDetail) {
                                case NONE -> suffix = " (" + shamanTotem.getTime() + " s)";
                                case COORDS -> {
                                    suffix = " (" + shamanTotem.getTime() + " s)";
                                    detail = " " + shamanTotem.getLocation().toString();
                                }
                                case DISTANCE -> suffix = " (" + shamanTotem.getTime() + " s, "
                                        + Math.round(McUtils.player()
                                                .position()
                                                .distanceTo(shamanTotem
                                                        .getLocation()
                                                        .toVec3()))
                                        + " m)";
                            }
                        }
                        // FIXME: textRenderSetting.withCustomColor is really bad allocation wise, consider using an
                        //        alternative
                        return new TextRenderTask(
                                getFormattedTotemText(prefix, suffix, detail),
                                textRenderSetting.withCustomColor(color));
                    })
                    .toList();
        }

        private String getFormattedTotemText(String prefix, String suffix, String detail) {
            String maxFitting = RenderedStringUtils.getMaxFittingText(
                    prefix + suffix + detail,
                    this.getWidth(),
                    FontRenderer.getInstance().getFont());
            if (maxFitting.contains("[")
                    && !maxFitting.contains("]")) { // Detail line did not appear to fit, force break
                return prefix + suffix + "\n" + detail;
            } else { // Fits fine, give normal lines
                return prefix + suffix + detail;
            }
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

    public enum TotemTrackingDetail {
        NONE,
        COORDS,
        DISTANCE
    }
}
