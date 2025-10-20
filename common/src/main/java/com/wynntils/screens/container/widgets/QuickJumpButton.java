/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.container.widgets;

import static com.wynntils.models.containers.BankModel.QUICK_JUMP_BUTTON_ICONS;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class QuickJumpButton extends WynntilsButton {
    private final int destination;
    private final CustomColor lockedColor;
    private final CustomColor selectedColor;
    private final PersonalStorageUtilitiesWidget parent;

    private int iconIndex;

    public QuickJumpButton(
            int x,
            int y,
            int destination,
            CustomColor lockedColor,
            CustomColor selectedColor,
            Integer iconIndex,
            PersonalStorageUtilitiesWidget parent) {
        super(x, y, 16, 16, Component.literal("Container Quick Jump Button"));

        this.destination = destination;
        this.lockedColor = lockedColor;
        this.selectedColor = selectedColor;
        this.iconIndex = iconIndex;
        this.parent = parent;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawHoverableTexturedRect(poseStack, Texture.QUICK_JUMP_BUTTON, getX(), getY(), isHovered);

        CustomColor color = CommonColors.WHITE;
        Component tooltip = Component.translatable(
                "feature.wynntils.personalStorageUtilities.jumpTo", Models.Bank.getPageName(destination));
        if (Models.Bank.getCurrentPage() == destination) {
            color = selectedColor;
            tooltip = Component.translatable("feature.wynntils.personalStorageUtilities.youAreHere");
        } else if (destination > Models.Bank.getFinalPage()) {
            color = lockedColor;
            tooltip = Component.translatable("feature.wynntils.personalStorageUtilities.unavailable", destination);
        }

        if (iconIndex != 0) {
            var index = iconIndex - 1;

            if (index >= 0 && index < QUICK_JUMP_BUTTON_ICONS.size()) {
                var texture = QUICK_JUMP_BUTTON_ICONS.get(index);
                RenderUtils.drawTexturedRectWithColor(
                        poseStack,
                        texture.resource(),
                        color,
                        getX(),
                        getY(),
                        0,
                        16,
                        16,
                        texture.width(),
                        texture.height());
            }
        } else {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(destination)),
                            getX() + 8,
                            getY() + 8,
                            color,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        if (isHovered) {
            McUtils.screen()
                    .setTooltipForNextRenderPass(Lists.transform(List.of(tooltip), Component::getVisualOrderText));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Models.Bank.isEditingMode()) {
            var maxIndex = QUICK_JUMP_BUTTON_ICONS.size() + 1;

            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                iconIndex = (iconIndex - 1 + maxIndex) % maxIndex;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                iconIndex = (iconIndex + 1) % maxIndex;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onPress() {
        if (!Models.Bank.isEditingMode()) {
            parent.jumpToPage(destination);
        }
    }

    public int getIconIndex() {
        return iconIndex;
    }

    public void setIconIndex(int iconIndex) {
        this.iconIndex = iconIndex;
    }
}
