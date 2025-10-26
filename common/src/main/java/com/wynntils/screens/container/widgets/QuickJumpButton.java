/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.container.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.containers.type.QuickJumpButtonIcon;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.RenderDirection;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class QuickJumpButton extends WynntilsButton {
    private final int destination;
    private final CustomColor lockedColor;
    private final CustomColor selectedColor;
    private final PersonalStorageUtilitiesWidget parent;

    private QuickJumpButtonIcon icon;

    public QuickJumpButton(
            int x,
            int y,
            int destination,
            CustomColor lockedColor,
            CustomColor selectedColor,
            QuickJumpButtonIcon icon,
            PersonalStorageUtilitiesWidget parent) {
        super(x, y, 16, 16, Component.literal("Container Quick Jump Button"));

        this.destination = destination;
        this.lockedColor = lockedColor;
        this.selectedColor = selectedColor;
        this.icon = icon;
        this.parent = parent;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics, Texture.QUICK_JUMP_BUTTON, getX(), getY(), isHovered, RenderDirection.VERTICAL);

        CustomColor color = CommonColors.WHITE;
        Component tooltip = Component.translatable(
                "feature.wynntils.personalStorageUtilities.jumpTo",
                Models.Bank.getPageCustomization(destination).getName());
        if (Models.Bank.getCurrentPage() == destination) {
            color = selectedColor;
            tooltip = Component.translatable("feature.wynntils.personalStorageUtilities.youAreHere");
        } else if (destination > Models.Bank.getFinalPage()) {
            color = lockedColor;
            tooltip = Component.translatable("feature.wynntils.personalStorageUtilities.unavailable", destination);
        }

        if (icon == QuickJumpButtonIcon.NONE) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(String.valueOf(destination)),
                            getX() + 8,
                            getY() + 8,
                            color,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

        } else {
            RenderUtils.drawTexturedRect(guiGraphics, this.icon.getTexture(), color, getX(), getY());
        }

        if (isHovered) {
            McUtils.screen()
                    .setTooltipForNextRenderPass(Lists.transform(List.of(tooltip), Component::getVisualOrderText));
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (Models.Bank.isEditingMode()) {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                icon = icon.next();
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                icon = icon.prev();
            }
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (!Models.Bank.isEditingMode()) {
            parent.jumpToPage(destination);
        }
    }

    public QuickJumpButtonIcon getIcon() {
        return icon;
    }

    public void setIcon(QuickJumpButtonIcon icon) {
        this.icon = icon;
    }
}
