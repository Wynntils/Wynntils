/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
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
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;

@ConfigCategory(Category.OVERLAYS)
public class InfoBoxFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox1Overlay = new InfoBoxOverlay(1);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox2Overlay = new InfoBoxOverlay(2);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox3Overlay = new InfoBoxOverlay(3);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox4Overlay = new InfoBoxOverlay(4);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox5Overlay = new InfoBoxOverlay(5);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox6Overlay = new InfoBoxOverlay(6);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox7Overlay = new InfoBoxOverlay(
            7,
            "%x% %y% %z%",
            new OverlayPosition(
                    160, 20, VerticalAlignment.Top, HorizontalAlignment.Left, OverlayPosition.AnchorSection.TopLeft),
            HorizontalAlignment.Center,
            VerticalAlignment.Middle,
            0);

    public static class InfoBoxOverlay extends Overlay {
        @Config
        public TextShadow textShadow = TextShadow.OUTLINE;

        @Config
        public String content = "";

        @Config
        public float secondsPerRecalculation = 0.5f;

        private final int id;
        private String[] cachedLines;
        private long lastUpdate = 0;

        protected InfoBoxOverlay(int id) {
            super(
                    new OverlayPosition(
                            -65 + (15 * id),
                            5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Left,
                            OverlayPosition.AnchorSection.MiddleLeft),
                    new GuiScaledOverlaySize(120, 10),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle);
            this.id = id;
        }

        protected InfoBoxOverlay(
                int id,
                String content,
                OverlayPosition position,
                HorizontalAlignment horizontalAlignment,
                VerticalAlignment verticalAlignment,
                float secondsPerRecalculation) {
            super(position, new GuiScaledOverlaySize(120, 10), horizontalAlignment, verticalAlignment);
            this.id = id;
            this.content = content;
            this.secondsPerRecalculation = secondsPerRecalculation;
        }

        @Override
        public void render(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            if (!Models.WorldState.onWorld()) return;

            if (System.nanoTime() - lastUpdate > secondsPerRecalculation * 1e+9) {
                lastUpdate = System.nanoTime();
                cachedLines = Managers.Function.doFormatLines(content);
            }

            float renderX = this.getRenderX();
            float renderY = this.getRenderY();
            for (String line : cachedLines) {
                BufferedFontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
                                bufferSource,
                                line,
                                renderX,
                                renderX + this.getWidth(),
                                renderY,
                                renderY + this.getHeight(),
                                0,
                                CommonColors.WHITE,
                                this.getRenderHorizontalAlignment(),
                                this.getRenderVerticalAlignment(),
                                this.textShadow);

                renderY += FontRenderer.getInstance().getFont().lineHeight;
            }
        }

        @Override
        public void renderPreview(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            if (!Models.WorldState.onWorld()) return;

            // FIXME: We do re-calculate this on render, but this is preview only, and fixing this would need a lot of
            //        architectural changes at the moment

            String[] renderedLines;
            if (content.isEmpty()) {
                renderedLines = Managers.Function.doFormatLines("&cX: {x}, &9Y: {y}, &aZ: {z}");
            } else {
                renderedLines = cachedLines;
            }

            float renderX = this.getRenderX();
            float renderY = this.getRenderY();
            for (String line : renderedLines) {
                BufferedFontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
                                bufferSource,
                                line,
                                renderX,
                                renderX + this.getWidth(),
                                renderY,
                                renderY + this.getHeight(),
                                0,
                                CommonColors.WHITE,
                                this.getRenderHorizontalAlignment(),
                                this.getRenderVerticalAlignment(),
                                this.textShadow);

                renderY += FontRenderer.getInstance().getFont().lineHeight;
            }
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}

        @Override
        public String getTranslatedName() {
            return I18n.get(
                    "feature.wynntils." + getDeclaringFeatureNameCamelCase() + ".overlay." + getNameCamelCase()
                            + ".name",
                    id);
        }

        @Override
        public String getConfigJsonName() {
            return super.getConfigJsonName() + id;
        }
    }
}
