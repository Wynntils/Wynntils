/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.chattabs.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.chattabs.ChatTabEditingScreen;
import com.wynntils.services.chat.type.ChatTab;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ChatTabsWidget extends AbstractWidget {
    private final float gridDivisions;
    private final ChatTab chatTab;
    private final Button deleteButton;
    private final Button moveUpButton;
    private final Button moveDownButton;
    private final ChatTabEditingScreen parent;

    public ChatTabsWidget(
            float x,
            float y,
            int width,
            int height,
            ChatTab chatTab,
            float gridDivisions,
            ChatTabEditingScreen parent) {
        super((int) x, (int) y, width, height, Component.literal(chatTab.name()));

        this.chatTab = chatTab;
        this.gridDivisions = gridDivisions;
        this.parent = parent;

        this.deleteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.chatTabsGui.delete")
                                .withStyle(ChatFormatting.RED),
                        (button) -> {
                            Services.ChatTab.removeTab(chatTab);
                            McUtils.setScreen(ChatTabEditingScreen.create());
                        })
                .pos((int) (this.getX() + (this.width / this.gridDivisions * 17)), this.getY() + (this.height / 2) - 10)
                .size((int) (this.width / gridDivisions * 5) - 3, 20)
                .build();
        this.moveUpButton = new Button.Builder(Component.literal("🠝"), (button) -> setChatTabIndex(-1))
                .pos((int) (this.getX() + (this.width / this.gridDivisions * 22)), this.getY() + (this.height / 2) - 10)
                .size((int) (this.width / gridDivisions * 2) - 2, 20)
                .build();
        this.moveDownButton = new Button.Builder(Component.literal("🠟"), (button) -> setChatTabIndex(1))
                .pos((int) (this.getX() + (this.width / this.gridDivisions * 24)), this.getY() + (this.height / 2) - 10)
                .size((int) (this.width / gridDivisions * 2) - 2, 20)
                .build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        if (this.isMouseOver(mouseX, mouseY)) {
            RenderUtils.drawRect(
                    poseStack, CommonColors.GRAY.withAlpha(70), this.getX(), this.getY(), 0, this.width, this.height);
        }

        CustomColor nameColor = parent.isActiveChatTab(chatTab) ? CommonColors.GREEN : CommonColors.WHITE;

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(chatTab.name()),
                        this.getX() + 4,
                        this.getY() + (this.height >> 1),
                        nameColor,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        moveUpButton.render(guiGraphics, mouseX, mouseY, partialTick);
        moveDownButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private void setChatTabIndex(int offset) {
        int newIndex =
                MathUtils.clamp(Services.ChatTab.getTabIndex(chatTab) + offset, 0, Services.ChatTab.getTabCount() - 1);

        Services.ChatTab.removeTab(chatTab);
        Services.ChatTab.addTab(newIndex, chatTab);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (deleteButton.mouseClicked(mouseX, mouseY, button)
                || moveUpButton.mouseClicked(mouseX, mouseY, button)
                || moveDownButton.mouseClicked(mouseX, mouseY, button)) {
            parent.reloadChatTabsWidgets();
            return true;
        }

        McUtils.setScreen(ChatTabEditingScreen.create(this.chatTab));
        return true;
    }
}
