/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.ChatTabEditingScreen;
import com.wynntils.gui.screens.WynntilsScreenWrapper;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public class ChatTabAddButton extends AbstractButton {
    public ChatTabAddButton(int x, int y, int width, int height) {
        super(x, y, width, height, new TextComponent("Chat Tab Add Button"));
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(poseStack, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), x, y, 0, width, height);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        "+",
                        x + 1,
                        x + width,
                        y + 1,
                        y + height,
                        0,
                        CommonColors.ORANGE,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.OUTLINE);
    }

    @Override
    public void onPress() {
        McUtils.mc().setScreen(WynntilsScreenWrapper.create(new ChatTabEditingScreen()));
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
