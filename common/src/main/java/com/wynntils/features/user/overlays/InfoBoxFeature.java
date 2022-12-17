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
import com.wynntils.core.functions.Function;
import com.wynntils.core.managers.Managers;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
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
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public String content = "";

        @Config
        public float secondsPerRecalculation = 0.5f;

        private final int id;
        private final List<Function<?>> functionDependencies = new ArrayList<>();
        private String[] cachedLines;
        private long lastUpdate = 0;

        protected InfoBoxOverlay(int id) {
            super(
                    new OverlayPosition(
                            -80 + (15 * id),
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
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            if (System.nanoTime() - lastUpdate > secondsPerRecalculation * 1e+9) {
                lastUpdate = System.nanoTime();
                cachedLines = Managers.Function.getLinesFromLegacyTemplate(content);
            }

            float renderX = this.getRenderX();
            float renderY = this.getRenderY();
            for (String line : cachedLines) {
                FontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
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
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            // FIXME: We do re-calculate this on render, but this is preview only, and fixing this would need a lot of
            //        architectural changes at the moment

            String[] renderedLines;
            if (content.isEmpty()) {
                renderedLines = Managers.Function.getLinesFromLegacyTemplate("&cX: %x%, &9Y: %y%, &aZ: %z%");
            } else {
                renderedLines = cachedLines;
            }

            float renderX = this.getRenderX();
            float renderY = this.getRenderY();
            for (String line : renderedLines) {
                FontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
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
        protected void onConfigUpdate(ConfigHolder configHolder) {
            for (Function<?> oldDependency : functionDependencies) {
                Managers.Function.disableFunction(oldDependency);
            }

            functionDependencies.clear();

            for (Function<?> function : Managers.Function.getDependenciesFromStringLegacy(content)) {
                Managers.Function.enableFunction(function);
            }
        }

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
