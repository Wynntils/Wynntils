/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.overlays.sizes.OverlaySize;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.render.buffered.BufferedFontRenderer;
import com.wynntils.gui.render.buffered.BufferedRenderUtils;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.objectives.WynnObjective;
import com.wynntils.utils.CommonColors;
import com.wynntils.utils.CustomColor;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class ObjectivesOverlayFeature extends UserFeature {
    private static final float SPACE_BETWEEN = 10;

    @Config
    public boolean disableObjectiveTrackingOnScoreboard = true;

    @Override
    public List<Model> getModelDependencies() {
        // FIXME: Should be ObjectivesManager when this has become a model
        return List.of();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScoreboardSegmentChange(ScoreboardSegmentAdditionEvent event) {
        if (disableObjectiveTrackingOnScoreboard) {
            ScoreboardSegment segment = event.getSegment();
            if (Managers.Objectives.isGuildObjectiveSegment(segment) && guildObjectiveOverlay.isEnabled()) {
                event.setCanceled(true);
                return;
            }
            if (Managers.Objectives.isObjectiveSegment(segment) && dailyObjectiveOverlay.isEnabled()) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final Overlay guildObjectiveOverlay = new GuildObjectiveOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final Overlay dailyObjectiveOverlay = new DailyObjectiveOverlay();

    public static class GuildObjectiveOverlay extends ObjectiveOverlayBase {
        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textColor")
        public CustomColor textColor = CommonColors.LIGHT_BLUE;

        protected GuildObjectiveOverlay() {
            super(
                    new OverlayPosition(
                            -5.5f,
                            -5,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.BottomRight),
                    new GuiScaledOverlaySize(150, 30),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle);
        }

        @Override
        public void render(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            WynnObjective guildObjective = Managers.Objectives.getGuildObjective();

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
            final float actualBarHeight = barHeight * (this.getWidth() / barWidth);
            final float renderedHeight = FontRenderer.getInstance()
                            .calculateRenderHeight(guildObjective.asObjectiveString(), this.getWidth())
                    + actualBarHeight;

            float renderY = this.getRenderY()
                    + switch (this.getRenderVerticalAlignment()) {
                        case Top -> 0;
                        case Middle -> (this.getHeight() - renderedHeight) / 2f;
                        case Bottom -> this.getHeight() - renderedHeight;
                    };

            final String text =
                    guildObjective.getGoal() + ": " + guildObjective.getScore() + "/" + guildObjective.getMaxScore();
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            text,
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            this.getWidth(),
                            this.textColor,
                            this.getRenderHorizontalAlignment(),
                            this.textShadow);

            float height = FontRenderer.getInstance().calculateRenderHeight(text, this.getWidth());

            if (height > 9) {
                renderY += height - 9;
            }

            if (this.enableProgressBar) {
                BufferedRenderUtils.drawProgressBar(
                        poseStack,
                        bufferSource,
                        Texture.BUBBLE_BAR,
                        this.getRenderX(),
                        renderY + SPACE_BETWEEN,
                        this.getRenderX() + this.getWidth(),
                        renderY + SPACE_BETWEEN + actualBarHeight,
                        0,
                        objectivesTexture.yOffset,
                        barWidth,
                        objectivesTexture.yOffset + 10,
                        guildObjective.getProgress());
            }
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    public static class DailyObjectiveOverlay extends ObjectiveOverlayBase {
        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textColor")
        public CustomColor textColor = CommonColors.GREEN;

        protected DailyObjectiveOverlay() {
            super(
                    new OverlayPosition(
                            -35,
                            -5,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.BottomRight),
                    new GuiScaledOverlaySize(150, 100),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Bottom);
        }

        @Override
        public void render(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            List<WynnObjective> objectives = Managers.Objectives.getPersonalObjectives();

            final int barHeight = this.enableProgressBar ? 5 : 0;
            final int barWidth = 182;
            final float actualBarHeight = barHeight * (this.getWidth() / barWidth);
            final float renderedHeightWithoutTextHeight = SPACE_BETWEEN + actualBarHeight;

            int tempHeight = 0;
            for (WynnObjective objective : objectives) {
                tempHeight += FontRenderer.getInstance()
                        .calculateRenderHeight(objective.asObjectiveString(), (int) this.getWidth());
            }

            final float fullHeight = renderedHeightWithoutTextHeight * objectives.size() - SPACE_BETWEEN + tempHeight;

            float offsetY =
                    switch (this.getRenderVerticalAlignment()) {
                        case Top -> 0;
                        case Middle -> (this.getHeight() - fullHeight) / 2f;
                        case Bottom -> this.getHeight() - fullHeight;
                    };

            for (WynnObjective objective : objectives) {
                if (this.hideOnInactivity) {
                    final int maxInactivityMs = 3000;
                    if (objective.getUpdatedAt() + maxInactivityMs < System.currentTimeMillis()) {
                        continue;
                    }
                }

                float renderY = offsetY + this.getRenderY();

                final String text = objective.asObjectiveString();
                BufferedFontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
                                bufferSource,
                                text,
                                this.getRenderX(),
                                this.getRenderX() + this.getWidth(),
                                renderY,
                                this.getWidth(),
                                this.textColor,
                                this.getRenderHorizontalAlignment(),
                                this.textShadow);

                final float textHeight = FontRenderer.getInstance().calculateRenderHeight(text, (int) this.getWidth());

                if (textHeight > 9) {
                    renderY += textHeight - 9;
                }

                if (this.enableProgressBar) {
                    BufferedRenderUtils.drawProgressBar(
                            poseStack,
                            bufferSource,
                            Texture.EXPERIENCE_BAR,
                            this.getRenderX(),
                            renderY + SPACE_BETWEEN,
                            this.getRenderX() + this.getWidth(),
                            renderY + SPACE_BETWEEN + actualBarHeight,
                            0,
                            objectivesTexture.yOffset,
                            barWidth,
                            objectivesTexture.yOffset + 10,
                            objective.getProgress());
                }

                offsetY += renderedHeightWithoutTextHeight + textHeight;
            }
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    protected abstract static class ObjectiveOverlayBase extends Overlay {
        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.hideOnInactivity")
        public boolean hideOnInactivity = false;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.enableProgressBar")
        public boolean enableProgressBar = true;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.objectivesTexture")
        public ObjectivesTextures objectivesTexture = ObjectivesTextures.a;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textShadow")
        public TextShadow textShadow = TextShadow.OUTLINE;

        protected ObjectiveOverlayBase(
                OverlayPosition position,
                OverlaySize size,
                HorizontalAlignment horizontalAlignmentOverride,
                VerticalAlignment verticalAlignmentOverride) {
            super(position, size, horizontalAlignmentOverride, verticalAlignmentOverride);
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
