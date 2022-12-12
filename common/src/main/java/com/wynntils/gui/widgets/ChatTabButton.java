/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.chat.tabs.ChatTab;
import com.wynntils.core.chat.tabs.ChatTabModel;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ChatTabButton extends AbstractButton {
    private final ChatTab tab;

    public ChatTabButton(int x, int y, int width, int height, ChatTab tab) {
        super(x, y, width, height, Component.literal("Chat Tab Button"));
        this.tab = tab;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (tab == null) return;

        RenderUtils.drawRect(
                poseStack,
                CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f),
                this.getX(),
                this.getY(),
                0,
                width,
                height);

        CustomColor color = ChatTabModel.getFocusedTab() == tab
                ? CommonColors.GREEN
                : (ChatTabModel.hasUnreadMessages(tab) ? CommonColors.YELLOW : CommonColors.WHITE);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        tab.getName(),
                        this.getX() + 1,
                        this.getX() + width,
                        this.getY() + 1,
                        this.getY() + height,
                        0,
                        color,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.OUTLINE);
    }

    @Override
    public void onPress() {
        ChatTabModel.setFocusedTab(tab);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
