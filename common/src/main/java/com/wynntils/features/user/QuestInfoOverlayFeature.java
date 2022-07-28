/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
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
import com.wynntils.wc.utils.scoreboard.quests.QuestInfo;
import com.wynntils.wc.utils.scoreboard.quests.QuestManager;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.language.I18n;

@FeatureInfo(category = "overlays")
public class QuestInfoOverlayFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay questInfoOverlay = new QuestInfoOverlay();

    public static class QuestInfoOverlay extends Overlay {
        private final static List<CustomColor> TEXT_COLORS = List.of(CommonColors.GREEN, CommonColors.ORANGE, CommonColors.WHITE);

        private final List<TextRenderTask> toRender = createRenderTaskList("", "");
        private final List<TextRenderTask> toRenderPreview = createRenderTaskList(
                I18n.get("feature.wynntils.questInfoOverlay.overlay.testQuestName") + ":",
                """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer tempus purus  \
                in lacus pulvinar dictum. Quisque suscipit erat pellentesque egestas volutpat. \
                """);

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

        private List<TextRenderTask> createRenderTaskList(String questName, String questDesc) {
            String[] texts = new String[] {
                I18n.get("feature.wynntils.questInfoOverlay.overlay.title") + ":",
                questName,
                questDesc
            };
            List<TextRenderTask> renderTaskList = new ArrayList<>(3);
            for (int i = 0; i < 3; i++) {
                renderTaskList.add(new TextRenderTask(
                        texts[i],
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
