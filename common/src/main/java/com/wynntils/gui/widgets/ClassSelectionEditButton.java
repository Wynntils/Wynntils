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
import com.wynntils.wynn.model.CharacterSelectionManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ClassSelectionEditButton extends AbstractButton {
    private static final List<Component> TOOLTIP = List.of(
            Component.translatable("screens.wynntils.characterSelection.edit.name")
                    .withStyle(ChatFormatting.YELLOW),
            Component.translatable("screens.wynntils.characterSelection.edit.discussion")
                    .withStyle(ChatFormatting.GRAY));
    private final CharacterSelectorScreen characterSelectorScreen;

    public ClassSelectionEditButton(
            int x, int y, int width, int height, CharacterSelectorScreen characterSelectorScreen) {
        super(x, y, width, height, Component.literal("Class Selection Edit Button"));
        this.characterSelectorScreen = characterSelectorScreen;
    }

    @Override
    public void onPress() {
        AbstractContainerMenu menu =
                characterSelectorScreen.getActualClassSelectionScreen().getMenu();

        CharacterSelectionManager.editCharacters(menu);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.EDIT_BUTTON.resource(),
                this.getX(),
                this.getY(),
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
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
