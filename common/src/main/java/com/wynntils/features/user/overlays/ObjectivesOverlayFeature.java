/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.google.common.collect.ImmutableList;
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
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.wc.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.wc.model.scoreboard.ScoreboardModel;
import com.wynntils.wc.model.scoreboard.objectives.ObjectiveHandler;
import com.wynntils.wc.model.scoreboard.objectives.WynnObjective;
import java.util.List;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ObjectivesOverlayFeature extends UserFeature {
    private static final float SPACE_BETWEEN = 10;

    @Config
    public static boolean disableObjectiveTrackingOnScoreboard = true;

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(ScoreboardModel.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScoreboardSegmentChange(ScoreboardSegmentAdditionEvent event) {
        if (disableObjectiveTrackingOnScoreboard) {
            if (event.getSegment().getType() == ScoreboardModel.SegmentType.GuildObjective
                    && guildObjectiveOverlay.isEnabled()) {
                event.setCanceled(true);
                return;
            }
            if (event.getSegment().getType() == ScoreboardModel.SegmentType.Objective
                    && dailyObjectiveOverlay.isEnabled()) {
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
                    new GuiScaledOverlaySize(150, 30),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle);
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            WynnObjective guildObjective = ObjectiveHandler.getGuildObjective();

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
            final float renderedHeight = 9 + actualBarHeight;

            float renderY = this.getRenderY()
                    + switch (this.getRenderVerticalAlignment()) {
                        case Top -> 0;
                        case Middle -> (this.getHeight() - renderedHeight) / 2f;
                        case Bottom -> this.getHeight() - renderedHeight;
                    };

            final String text =
                    guildObjective.getGoal() + ": " + guildObjective.getScore() + "/" + guildObjective.getMaxScore();
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            text,
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            this.getWidth(),
                            this.textColor,
                            FontRenderer.TextAlignment.fromHorizontalAlignment(this.getRenderHorizontalAlignment()),
                            this.textShadow);

            float height = FontRenderer.getInstance().calculateRenderHeight(List.of(text), this.getWidth());

            if (height > 9) {
                renderY += height - 9;
            }

            if (this.enableProgressBar) {
                RenderUtils.drawProgressBar(
                        poseStack,
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
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            render(poseStack, partialTicks, window);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    public static class DailyObjectiveOverlay extends ObjectiveOverlayBase {
        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textColour")
        public CustomColor textColor = CommonColors.GREEN;

        public DailyObjectiveOverlay() {
            super(
                    new OverlayPosition(
                            -35.5f,
                            -5.0f,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.BottomRight),
                    new GuiScaledOverlaySize(150, 100),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Bottom);
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            List<WynnObjective> objectives = ObjectiveHandler.getObjectives();

            final float SPACE_BETWEEN = 10;

            final int barHeight = this.enableProgressBar ? 5 : 0;
            final int barWidth = 182;
            final float actualBarHeight = barHeight * (this.getWidth() / barWidth);
            final float renderedHeight = SPACE_BETWEEN + 9 + actualBarHeight;
            final float fullHeight = renderedHeight * objectives.size() - SPACE_BETWEEN;

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

                final String text = objective.getGoal() + ": " + objective.getScore() + "/" + objective.getMaxScore();
                FontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
                                text,
                                this.getRenderX(),
                                this.getRenderX() + this.getWidth(),
                                renderY,
                                this.getWidth(),
                                this.textColor,
                                FontRenderer.TextAlignment.fromHorizontalAlignment(this.getRenderHorizontalAlignment()),
                                this.textShadow);

                float height = FontRenderer.getInstance().calculateRenderHeight(List.of(text), this.getWidth());

                if (height > 9) {
                    offsetY += height - 9;
                    renderY += height - 9;
                }

                if (this.enableProgressBar) {
                    RenderUtils.drawProgressBar(
                            poseStack,
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

                offsetY += renderedHeight;
            }
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            render(poseStack, partialTicks, window);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    public abstract static class ObjectiveOverlayBase extends Overlay {
        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.hideOnInactivity")
        public boolean hideOnInactivity = false;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.enableProgressBar")
        public boolean enableProgressBar = true;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.objectivesTexture")
        public ObjectivesTextures objectivesTexture = ObjectivesTextures.a;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textShadow")
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

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
