/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.managers.Managers;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.CharacterSelectorScreen;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ClassSelectionAddButton extends WynntilsButton {
    private static final List<Component> TOOLTIP_CANNOT_ADD = List.of(
            Component.translatable("screens.wynntils.characterSelection.cannotAdd.name")
                    .withStyle(ChatFormatting.DARK_RED),
            Component.translatable("screens.wynntils.characterSelection.cannotAdd.discussion")
                    .withStyle(ChatFormatting.GRAY));
    private static final List<Component> TOOLTIP_CAN_ADD = List.of(
            Component.translatable("screens.wynntils.characterSelection.add.name")
                    .withStyle(ChatFormatting.GREEN),
            Component.translatable("screens.wynntils.characterSelection.add.discussion")
                    .withStyle(ChatFormatting.GRAY));
    private final CharacterSelectorScreen characterSelectorScreen;

    public ClassSelectionAddButton(
            int x, int y, int width, int height, CharacterSelectorScreen characterSelectorScreen) {
        super(x, y, width, height, Component.literal("Class Selection Delete Button"));
        this.characterSelectorScreen = characterSelectorScreen;
    }

    @Override
    public void onPress() {
        if (characterSelectorScreen.getFirstNewCharacterSlot() == -1) return;

        Managers.CharacterSelection.createNewClass();
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.ADD_BUTTON.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                characterSelectorScreen.getFirstNewCharacterSlot() == -1 ? Texture.ADD_BUTTON.height() / 2 : 0,
                Texture.ADD_BUTTON.width(),
                Texture.ADD_BUTTON.height() / 2,
                Texture.ADD_BUTTON.width(),
                Texture.ADD_BUTTON.height());

        if (isHovered) {
            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    100,
                    characterSelectorScreen.getFirstNewCharacterSlot() == -1 ? TOOLTIP_CANNOT_ADD : TOOLTIP_CAN_ADD,
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }
}
