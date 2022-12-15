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
import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.wynn.event.TrackedQuestUpdateEvent;
import com.wynntils.wynn.model.quests.QuestInfo;
import com.wynntils.wynn.model.scoreboard.ScoreboardModel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class QuestInfoOverlayFeature extends UserFeature {
    @Config
    public boolean disableQuestTrackingOnScoreboard = true;

    @Config
    public boolean autoTrackQuestCoordinates = true;

    @Override
    public List<? extends Model> getModelDependencies() {
        return List.of(Models.Scoreboard, Models.Compass);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScoreboardSegmentChange(ScoreboardSegmentAdditionEvent event) {
        if (questInfoOverlay.isEnabled()
                && disableQuestTrackingOnScoreboard
                && event.getSegment().getType() == ScoreboardModel.SegmentType.Quest) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTrackedQuestUpdate(TrackedQuestUpdateEvent event) {
        if (!autoTrackQuestCoordinates) return;

        // set if valid
        Models.Compass.setDynamicCompassLocation(Managers.Quest::getCurrentQuestLocation);
    }

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay questInfoOverlay = new QuestInfoOverlay();

    public static class QuestInfoOverlay extends Overlay {
        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        private static final List<CustomColor> TEXT_COLORS =
                List.of(CommonColors.GREEN, CommonColors.ORANGE, CommonColors.WHITE);

        private final List<TextRenderTask> toRender = createRenderTaskList();
        private final List<TextRenderTask> toRenderPreview = createRenderTaskList();

        protected QuestInfoOverlay() {
            super(
                    new OverlayPosition(
                            5,
                            -5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.TopRight),
                    new GuiScaledOverlaySize(300, 50),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle);

            toRender.get(0).setText(I18n.get("feature.wynntils.questInfoOverlay.overlay.questInfo.title") + ":");

            toRenderPreview.get(0).setText(I18n.get("feature.wynntils.questInfoOverlay.overlay.questInfo.title") + ":");
            toRenderPreview
                    .get(1)
                    .setText(I18n.get("feature.wynntils.questInfoOverlay.overlay.questInfo.testQuestName") + ":");
            toRenderPreview
                    .get(2)
                    .setText(
                            """
                            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer \
                            tempus purus in lacus pulvinar dictum. Quisque suscipit erat \
                            pellentesque egestas volutpat. \
                            """);
        }

        private List<TextRenderTask> createRenderTaskList() {
            List<TextRenderTask> renderTaskList = new ArrayList<>(3);
            for (int i = 0; i < 3; i++) {
                renderTaskList.add(new TextRenderTask(
                        null,
                        TextRenderSetting.DEFAULT
                                .withMaxWidth(this.getWidth())
                                .withCustomColor(TEXT_COLORS.get(i))
                                .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                                .withTextShadow(this.textShadow)));
            }
            return renderTaskList;
        }

        private void updateTextRenderSettings(List<TextRenderTask> renderTasks) {
            for (int i = 0; i < 3; i++) {
                renderTasks
                        .get(i)
                        .setSetting(TextRenderSetting.DEFAULT
                                .withMaxWidth(this.getWidth())
                                .withCustomColor(TEXT_COLORS.get(i))
                                .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                                .withTextShadow(this.textShadow));
            }
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            updateTextRenderSettings(toRender);
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            QuestInfo currentQuest = Managers.Quest.getCurrentQuest();

            if (currentQuest == null) {
                return;
            }

            toRender.get(1).setText(currentQuest.getName());
            toRender.get(2).setText(currentQuest.getNextTask());

            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            toRender,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            updateTextRenderSettings(toRenderPreview); // we have to force update every time

            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            toRenderPreview,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }
    }
}
