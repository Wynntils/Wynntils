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
import com.wynntils.core.functions.FunctionManager;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class InfoBoxFeature extends UserFeature {

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox1Overlay = new InfoBoxOverlay(1);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox2Overlay = new InfoBoxOverlay(2);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox3Overlay = new InfoBoxOverlay(3);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox4Overlay = new InfoBoxOverlay(4);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox5Overlay = new InfoBoxOverlay(5);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox6Overlay = new InfoBoxOverlay(6);

    public static class InfoBoxOverlay extends Overlay {
        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public String content = "";

        @Config
        public float secondsPerRecalculation = 0.5f;

        private final int id;
        private final List<Function<?>> functionDependencies = new ArrayList<>();
        private TextRenderTask renderTask;
        private long lastUpdate = 0;

        private TextRenderTask getRenderTask(String content) {
            return FunctionManager.getStringFromLegacyTemplate(content)
                    .setSetting(TextRenderSetting.DEFAULT
                            .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                            .withMaxWidth(this.getWidth())
                            .withCustomColor(CustomColor.fromChatFormatting(ChatFormatting.WHITE))
                            .withTextShadow(textShadow));
        }

        protected InfoBoxOverlay(int id) {
            super(
                    new OverlayPosition(
                            -60 + (15 * id),
                            5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Left,
                            OverlayPosition.AnchorSection.MiddleLeft),
                    new GuiScaledOverlaySize(120, 10),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle);
            this.id = id;
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            if (System.nanoTime() - lastUpdate > secondsPerRecalculation * 1e+9) {
                lastUpdate = System.nanoTime();
                renderTask = getRenderTask(content);
            }

            FontRenderer.getInstance()
                    .renderTextWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            renderTask,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            // FIXME: We do re-calculate this on render, but this is preview only, and fixing this would need a lot of
            //        architectural changes at the moment
            TextRenderTask toRenderPreview = getRenderTask("&cX: %x%, &9Y: %y%, &aZ: %z%");

            FontRenderer.getInstance()
                    .renderTextWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            toRenderPreview,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            for (Function<?> oldDependency : functionDependencies) {
                FunctionManager.disableFunction(oldDependency);
            }

            functionDependencies.clear();

            for (Function<?> function : FunctionManager.getDependenciesFromStringLegacy(content)) {
                FunctionManager.enableFunction(function);
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
