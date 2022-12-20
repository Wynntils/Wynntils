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
import com.wynntils.gui.screens.ChatTabEditingScreen;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ChatTabButton extends WynntilsButton {
    private final ChatTab tab;

    public ChatTabButton(int x, int y, int width, int height, ChatTab tab) {
        super(x, y, width, height, Component.literal("Chat Tab Button"));
        this.tab = tab;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (tab == null) return;

        RenderUtils.drawRect(
                poseStack, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), 0, width, height);

        CustomColor color = Models.ChatTab.getFocusedTab() == tab
                ? CommonColors.GREEN
                : (Models.ChatTab.hasUnreadMessages(tab) ? CommonColors.YELLOW : CommonColors.WHITE);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        tab.getName(),
                        getX() + 1,
                        getX() + width,
                        getY() + 1,
                        getY() + height,
                        0,
                        color,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.OUTLINE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Models.ChatTab.setFocusedTab(tab);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            McUtils.mc().setScreen(ChatTabEditingScreen.create(tab));
        }
        return true;
    }

    // unused
    @Override
    public void onPress() {}
}
