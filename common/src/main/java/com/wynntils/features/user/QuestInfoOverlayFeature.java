/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.wc.utils.scoreboard.quests.QuestInfo;
import com.wynntils.wc.utils.scoreboard.quests.QuestManager;
import java.util.ArrayList;
import java.util.List;

@FeatureInfo(category = "overlays")
public class QuestInfoOverlayFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay QuestInfoOverlay = new QuestInfoOverlay();

    public static class QuestInfoOverlay extends Overlay {
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

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            QuestInfo currentQuest = QuestManager.getCurrentQuest();

            if (currentQuest == null) {
                return;
            }

            List<TextRenderTask> toRender = new ArrayList<>();

            toRender.add(TextRenderTask.getWithHorizontalAlignment(
                    "Tracked Quest Info:", this.getWidth(), CommonColors.GREEN, this.getRenderHorizontalAlignment()));
            toRender.add(TextRenderTask.getWithHorizontalAlignment(
                    currentQuest.quest(), this.getWidth(), CommonColors.ORANGE, this.getRenderHorizontalAlignment()));
            toRender.add(TextRenderTask.getWithHorizontalAlignment(
                    currentQuest.description(),
                    this.getWidth(),
                    CommonColors.WHITE,
                    this.getRenderHorizontalAlignment()));

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
    }
}
