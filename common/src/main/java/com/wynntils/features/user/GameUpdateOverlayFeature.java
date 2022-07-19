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
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.utils.objects.CommonColors;

@FeatureInfo(category = "overlays")
public class GameUpdateOverlayFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay QuestInfoOverlay = new GameUpdateOverlay();

    public static class GameUpdateOverlay extends Overlay {

        @Config
        public int messageLimit = 5;

        @Config
        public float messageTimeLimit = 10f;

        @Config
        public boolean invertGrowth = true;

        @Config
        public int messageMaxLength = 0;

        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public boolean overrideNewMessages = true;

        public GameUpdateOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.BottomRight),
                    500,
                    100);
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRectBorders(
                    poseStack,
                    CommonColors.WHITE,
                    this.getRenderX(),
                    this.getRenderY(),
                    this.getRenderX() + getRenderedWidth(),
                    this.getRenderY() + this.getRenderedHeight(),
                    0,
                    2);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }
}
