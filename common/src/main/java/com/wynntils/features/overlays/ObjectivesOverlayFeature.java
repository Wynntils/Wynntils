/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.Overlay;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.objectives.DailyObjectiveScoreboardPart;
import com.wynntils.models.objectives.GuildObjectiveScoreboardPart;
import com.wynntils.models.objectives.WynnObjective;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.ObjectivesTextures;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class ObjectivesOverlayFeature extends Feature {
    private static final float SPACE_BETWEEN = 10;

    @RegisterConfig
    public final Config<Boolean> disableObjectiveTrackingOnScoreboard = new Config<>(true);

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScoreboardSegmentChange(ScoreboardSegmentAdditionEvent event) {
        if (disableObjectiveTrackingOnScoreboard.get()) {
            ScoreboardSegment segment = event.getSegment();
            if (segment.getScoreboardPart() instanceof DailyObjectiveScoreboardPart
                    && Managers.Overlay.isEnabled(dailyObjectiveOverlay)) {
                event.setCanceled(true);
                return;
            }
            if (segment.getScoreboardPart() instanceof GuildObjectiveScoreboardPart
                    && Managers.Overlay.isEnabled(guildObjectiveOverlay)) {
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
        @RegisterConfig("feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textColor")
        public final Config<CustomColor> textColor = new Config<>(CommonColors.LIGHT_BLUE);

        protected GuildObjectiveOverlay() {
            super(
                    new OverlayPosition(
                            -5.5f,
                            -5,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.RIGHT,
                            OverlayPosition.AnchorSection.BOTTOM_RIGHT),
                    new OverlaySize(150, 30),
                    HorizontalAlignment.LEFT,
                    VerticalAlignment.MIDDLE);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
            WynnObjective guildObjective = Models.Objectives.getGuildObjective();

            if (guildObjective == null) return;

            if (this.hideOnInactivity.get()) {
                final int maxInactivityMs = 3000;
                if (guildObjective.getUpdatedAt() + maxInactivityMs < System.currentTimeMillis()) {
                    return;
                }
            }

            final int barHeight = this.enableProgressBar.get() ? 5 : 0;
            final int barWidth = 182;
            final float actualBarHeight = barHeight * (this.getWidth() / barWidth);
            final float renderedHeight = FontRenderer.getInstance()
                            .calculateRenderHeight(guildObjective.asObjectiveString(), this.getWidth())
                    + actualBarHeight;

            float renderY = this.getRenderY()
                    + switch (this.getRenderVerticalAlignment()) {
                        case TOP -> 0;
                        case MIDDLE -> (this.getHeight() - renderedHeight) / 2f;
                        case BOTTOM -> this.getHeight() - renderedHeight;
                    };

            final String text = guildObjective.asObjectiveString();
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            StyledText.fromString(text),
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            this.getWidth(),
                            this.textColor.get(),
                            this.getRenderHorizontalAlignment(),
                            this.textShadow.get());

            float height = FontRenderer.getInstance().calculateRenderHeight(text, this.getWidth());

            if (height > 9) {
                renderY += height - 9;
            }

            if (this.enableProgressBar.get()) {
                BufferedRenderUtils.drawProgressBar(
                        poseStack,
                        bufferSource,
                        Texture.BUBBLE_BAR,
                        this.getRenderX(),
                        renderY + SPACE_BETWEEN,
                        this.getRenderX() + this.getWidth(),
                        renderY + SPACE_BETWEEN + actualBarHeight,
                        0,
                        objectivesTexture.get().getTextureYOffset(),
                        barWidth,
                        objectivesTexture.get().getTextureYOffset() + 10,
                        guildObjective.getProgress());
            }
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    public static class DailyObjectiveOverlay extends ObjectiveOverlayBase {
        @RegisterConfig("feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textColor")
        public final Config<CustomColor> textColor = new Config<>(CommonColors.GREEN);

        protected DailyObjectiveOverlay() {
            super(
                    new OverlayPosition(
                            -35,
                            -5,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.RIGHT,
                            OverlayPosition.AnchorSection.BOTTOM_RIGHT),
                    new OverlaySize(150, 100),
                    HorizontalAlignment.LEFT,
                    VerticalAlignment.BOTTOM);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
            List<WynnObjective> objectives = Models.Objectives.getPersonalObjectives();

            if (objectives.isEmpty()) return;

            final int barHeight = this.enableProgressBar.get() ? 5 : 0;
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
                        case TOP -> 0;
                        case MIDDLE -> (this.getHeight() - fullHeight) / 2f;
                        case BOTTOM -> this.getHeight() - fullHeight;
                    };

            for (WynnObjective objective : objectives) {
                if (this.hideOnInactivity.get()) {
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
                                StyledText.fromString(text),
                                this.getRenderX(),
                                this.getRenderX() + this.getWidth(),
                                renderY,
                                this.getWidth(),
                                this.textColor.get(),
                                this.getRenderHorizontalAlignment(),
                                this.textShadow.get());

                final float textHeight = FontRenderer.getInstance().calculateRenderHeight(text, (int) this.getWidth());

                if (textHeight > 9) {
                    renderY += textHeight - 9;
                }

                if (this.enableProgressBar.get()) {
                    BufferedRenderUtils.drawProgressBar(
                            poseStack,
                            bufferSource,
                            Texture.EXPERIENCE_BAR,
                            this.getRenderX(),
                            renderY + SPACE_BETWEEN,
                            this.getRenderX() + this.getWidth(),
                            renderY + SPACE_BETWEEN + actualBarHeight,
                            0,
                            objectivesTexture.get().getTextureYOffset(),
                            barWidth,
                            objectivesTexture.get().getTextureYOffset() + 10,
                            objective.getProgress());
                }

                offsetY += renderedHeightWithoutTextHeight + textHeight;
            }
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    protected abstract static class ObjectiveOverlayBase extends Overlay {
        @RegisterConfig("feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.hideOnInactivity")
        public final Config<Boolean> hideOnInactivity = new Config<>(false);

        @RegisterConfig("feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.enableProgressBar")
        public final Config<Boolean> enableProgressBar = new Config<>(true);

        @RegisterConfig("overlay.wynntils.objectivesTexture")
        public final Config<ObjectivesTextures> objectivesTexture = new Config<>(ObjectivesTextures.A);

        @RegisterConfig("feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textShadow")
        public final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

        protected ObjectiveOverlayBase(
                OverlayPosition position,
                OverlaySize size,
                HorizontalAlignment horizontalAlignmentOverride,
                VerticalAlignment verticalAlignmentOverride) {
            super(position, size, horizontalAlignmentOverride, verticalAlignmentOverride);
        }
    }
}
