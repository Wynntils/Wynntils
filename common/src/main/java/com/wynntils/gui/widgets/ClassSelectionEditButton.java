/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.CharacterSelectorScreen;
import com.wynntils.wynn.utils.ContainerUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;

public class ClassSelectionEditButton extends AbstractButton {
    private static final List<Component> TOOLTIP = List.of(
            new TranslatableComponent("screens.wynntils.characterSelection.edit.name").withStyle(ChatFormatting.YELLOW),
            new TranslatableComponent("screens.wynntils.characterSelection.edit.discussion")
                    .withStyle(ChatFormatting.GRAY));
    private static final int EDIT_BUTTON_SLOT = 8;
    private final CharacterSelectorScreen characterSelectorScreen;

    public ClassSelectionEditButton(
            int x, int y, int width, int height, CharacterSelectorScreen characterSelectorScreen) {
        super(x, y, width, height, new TextComponent("Class Selection Edit Button"));
        this.characterSelectorScreen = characterSelectorScreen;
    }

    @Override
    public void onPress() {
        AbstractContainerMenu menu =
                characterSelectorScreen.getActualClassSelectionScreen().getMenu();

        ContainerUtils.clickOnSlot(EDIT_BUTTON_SLOT, menu.containerId, GLFW.GLFW_MOUSE_BUTTON_LEFT, menu.getItems());
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.EDIT_BUTTON.resource(),
                this.x,
                this.y,
                0,
                this.width,
                this.height,
                Texture.EDIT_BUTTON.width(),
                Texture.EDIT_BUTTON.height());

        if (isHovered) {
            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    100,
                    TOOLTIP,
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
