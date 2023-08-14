/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.chattabs.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.chattabs.ChatTabEditingScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.network.chat.Component;

public class ChatTabAddButton extends WynntilsButton {
    public ChatTabAddButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Chat Tab Add Button"));
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                poseStack, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), 0, width, height);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString("+"),
                        getX() + 1,
                        getX() + width,
                        getY() + 1,
                        getY() + height,
                        0,
                        CommonColors.ORANGE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }

    @Override
    public void onPress() {
        McUtils.mc().setScreen(ChatTabEditingScreen.create());
    }
}
