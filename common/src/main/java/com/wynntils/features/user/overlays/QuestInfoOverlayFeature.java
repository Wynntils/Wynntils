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
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.wc.utils.scoreboard.ScoreboardManager;
import com.wynntils.wc.utils.scoreboard.quests.QuestInfo;
import com.wynntils.wc.utils.scoreboard.quests.QuestManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = "Overlays")
public class QuestInfoOverlayFeature extends UserFeature {

    @Config
    public static boolean disableQuestTrackingOnScoreboard = true;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScoreboardSegmentChange(ScoreboardSegmentAdditionEvent event) {
        if (questInfoOverlay.isEnabled()
                && disableQuestTrackingOnScoreboard
                && event.getSegment().getType() == ScoreboardManager.SegmentType.Quest) {
            event.setCanceled(true);
        }
    }

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay questInfoOverlay = new QuestInfoOverlay();

    public static class QuestInfoOverlay extends Overlay {
        private static final List<CustomColor> TEXT_COLORS =
                List.of(CommonColors.GREEN, CommonColors.ORANGE, CommonColors.WHITE);

        private final List<TextRenderTask> toRender = createRenderTaskList();
        private final List<TextRenderTask> toRenderPreview = createRenderTaskList();

        public QuestInfoOverlay() {
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
        }

        private List<TextRenderTask> createRenderTaskList() {
            List<TextRenderTask> renderTaskList = new ArrayList<>(3);
            for (int i = 0; i < 3; i++) {
                renderTaskList.add(new TextRenderTask(
                        null,
                        TextRenderSetting.getWithHorizontalAlignment(
                                this.getWidth(), TEXT_COLORS.get(i), this.getRenderHorizontalAlignment())));
            }
            return renderTaskList;
        }

        private void updateTextRenderSettings(List<TextRenderTask> renderTasks) {
            for (int i = 0; i < 3; i++) {
                renderTasks
                        .get(i)
                        .setSetting(TextRenderSetting.getWithHorizontalAlignment(
                                this.getWidth(), TEXT_COLORS.get(i), this.getRenderHorizontalAlignment()));
            }
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            updateTextRenderSettings(toRender);
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            QuestInfo currentQuest = QuestManager.getCurrentQuest();

            if (currentQuest == null) {
                return;
            }

            if (toRender.get(0).getText() == null) {
                // Set at first use; I18n is not available at initialization time
                toRender.get(0).setText(I18n.get("feature.wynntils.questInfoOverlay.overlay.questInfo.title") + ":");
            }
            toRender.get(1).setText(currentQuest.quest());
            toRender.get(2).setText(currentQuest.description());

            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            toRender,
                            this.getRenderedWidth(),
                            this.getRenderedHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            if (toRenderPreview.get(0).getText() == null) {
                // Set at first use; I18n is not available at initialization time
                toRenderPreview
                        .get(0)
                        .setText(I18n.get("feature.wynntils.questInfoOverlay.overlay.questInfo.title") + ":");
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
            updateTextRenderSettings(toRenderPreview); // we have to force update every time

            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            toRenderPreview,
                            this.getRenderedWidth(),
                            this.getRenderedHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }
    }
}
