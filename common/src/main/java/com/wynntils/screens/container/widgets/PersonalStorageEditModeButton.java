/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.container.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class PersonalStorageEditModeButton extends WynntilsButton {
    private static final List<Component> CONFIRM_TOOLTIP = List.of(
            Component.translatable("screens.wynntils.containers.save.name").withStyle(ChatFormatting.GREEN),
            Component.translatable("screens.wynntils.containers.save.description")
                    .withStyle(ChatFormatting.GRAY));

    private static final List<Component> EDIT_TOOLTIP = List.of(
            Component.translatable("screens.wynntils.containers.edit.name").withStyle(ChatFormatting.YELLOW),
            Component.translatable("screens.wynntils.containers.edit.description")
                    .withStyle(ChatFormatting.GRAY));

    private final PersonalStorageUtilitiesWidget parent;

    public PersonalStorageEditModeButton(int x, int y, int width, int height, PersonalStorageUtilitiesWidget parent) {
        super(x, y, width, height, Component.literal("Personal Storage Edit Name Button"));

        this.parent = parent;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.EDIT_NAME_ICON.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                Texture.EDIT_NAME_ICON.width(),
                Texture.EDIT_NAME_ICON.height());

        if (isHovered) {
            List<Component> tooltipToUse = Models.Bank.isEditingMode() ? CONFIRM_TOOLTIP : EDIT_TOOLTIP;

            McUtils.screen().setTooltipForNextRenderPass(Lists.transform(tooltipToUse, Component::getVisualOrderText));
        }
    }

    // unused
    @Override
    public void onPress() {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (Models.Bank.isEditingMode()) {
                parent.saveEditModeChanges();
                parent.updatePageName();
                parent.toggleEditMode(false);
            } else {
                parent.toggleEditMode(true);
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (Models.Bank.isEditingMode()) {
                parent.toggleEditMode(false);
                parent.updatePageIcons();
            } else {
                Models.Bank.resetCurrentPageName();
                parent.updatePageName();
            }
        }

        return true;
    }
}
