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
import com.wynntils.core.features.overlays.sizes.OverlaySize;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.utils.scoreboard.objectives.ObjectiveManager;
import com.wynntils.wc.utils.scoreboard.objectives.WynnObjective;

public class ObjectivesOverlayFeature extends UserFeature {

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final Overlay guildObjectiveOverlay = new GuildObjectiveOverlay();

    public static class GuildObjectiveOverlay extends ObjectiveOverlay {

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textColour")
        public CustomColor textColor = CommonColors.LIGHT_BLUE;

        public GuildObjectiveOverlay() {
            super(
                    new OverlayPosition(
                            -5.5f,
                            -5.0f,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.BottomRight),
                    new GuiScaledOverlaySize(150, 30));
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            WynnObjective guildObjective = ObjectiveManager.getGuildObjective();

            if (guildObjective == null) {
                return;
            }

            if (this.hideOnInactivity) {
                final int maxInactivityMs = 3000;
                if (guildObjective.getUpdatedAt() + maxInactivityMs < System.currentTimeMillis()) {
                    return;
                }
            }

            final int barHeight = this.enableProgressBar ? 5 : 0;
            final int barWidth = 182;
            final float renderedHeight = 5 + barHeight * (this.getWidth() / barWidth + 1);

            float renderY =
                    switch (this.getRenderVerticalAlignment()) {
                        case Top -> this.getRenderY();
                        case Middle -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
                        case Bottom -> this.getRenderY() + this.getHeight() - renderedHeight;
                    };

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            guildObjective.getGoal() + ": " + guildObjective.getScore() + "/"
                                    + guildObjective.getMaxScore(),
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            0,
                            this.textColor,
                            FontRenderer.TextAlignment.fromHorizontalAlignment(this.getRenderHorizontalAlignment()),
                            this.textShadow);

            if (this.enableProgressBar) {
                RenderUtils.drawProgressBar(
                        poseStack,
                        Texture.BUBBLE_BAR,
                        this.getRenderX(),
                        renderY + 10,
                        this.getRenderX() + this.getWidth(),
                        renderY + 10 + barHeight * (this.getWidth() / barWidth),
                        0,
                        objectivesTexture.yOffset,
                        barWidth,
                        objectivesTexture.yOffset + 10,
                        guildObjective.getProgress());
            }
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            render(poseStack, partialTicks, window);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    public abstract static class ObjectiveOverlay extends Overlay {

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.hideOnInactivity")
        public boolean hideOnInactivity = false;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.enableProgressBar")
        public boolean enableProgressBar = true;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.objectivesTexture")
        public ObjectivesTextures objectivesTexture = ObjectivesTextures.a;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textShadow")
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        public ObjectiveOverlay(OverlayPosition position, OverlaySize size) {
            super(position, size);
        }
    }

    public enum ObjectivesTextures {
        Wynn(0),
        Liquid(40),
        Emerald(50),
        a(10),
        b(20),
        c(30);

        private final int yOffset;

        ObjectivesTextures(int yOffset) {
            this.yOffset = yOffset;
        }

        public int getTextureYOffset() {
            return yOffset;
        }
    }
}
