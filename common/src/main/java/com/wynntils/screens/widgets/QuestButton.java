/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.screens.WynntilsQuestBookScreen;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.model.questbook.QuestInfo;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public class QuestButton extends AbstractButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);

    private final QuestInfo questInfo;
    private final WynntilsQuestBookScreen screen;

    public QuestButton(int x, int y, int width, int height, QuestInfo questInfo, WynntilsQuestBookScreen screen) {
        super(x, y, width, height, new TextComponent("Quest Button"));
        this.questInfo = questInfo;
        this.screen = screen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor backgroundColor = this.isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR;

        RenderUtils.drawRect(poseStack, backgroundColor, this.x, this.y, 0, this.width, this.height);

        int maxTextWidth = this.width - 10 - 11;
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StringUtils.getMaxFittingText(
                                questInfo.getName(),
                                maxTextWidth,
                                FontRenderer.getInstance().getFont()),
                        this.x + 14,
                        this.y + 1,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NONE);

        Texture stateTexture =
                switch (questInfo.getStatus()) {
                    case STARTED -> Texture.QUEST_STARTED;
                    case COMPLETED -> Texture.QUEST_FINISHED;
                    case CAN_START -> Texture.QUEST_CAN_START;
                    case CANNOT_START -> Texture.QUEST_CANNOT_START;
                };

        RenderUtils.drawTexturedRect(
                poseStack,
                stateTexture.resource(),
                this.x + 1,
                this.y + 1,
                stateTexture.width(),
                stateTexture.height(),
                stateTexture.width(),
                stateTexture.height());
    }

    @Override
    public void onPress() {}

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    public QuestInfo getQuestInfo() {
        return questInfo;
    }
}
