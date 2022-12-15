/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.chat.tabs.ChatTab;
import com.wynntils.core.managers.Models;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public class ChatTabButton extends AbstractButton {
    private final ChatTab tab;

    public ChatTabButton(int x, int y, int width, int height, ChatTab tab) {
        super(x, y, width, height, new TextComponent("Chat Tab Button"));
        this.tab = tab;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (tab == null) return;

        RenderUtils.drawRect(poseStack, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), x, y, 0, width, height);

        CustomColor color = Models.ChatTab.getFocusedTab() == tab
                ? CommonColors.GREEN
                : (Models.ChatTab.hasUnreadMessages(tab) ? CommonColors.YELLOW : CommonColors.WHITE);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        tab.getName(),
                        x + 1,
                        x + width,
                        y + 1,
                        y + height,
                        0,
                        color,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.OUTLINE);
    }

    @Override
    public void onPress() {
        Models.ChatTab.setFocusedTab(tab);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
