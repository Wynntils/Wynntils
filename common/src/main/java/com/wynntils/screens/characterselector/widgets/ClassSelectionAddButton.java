/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.characterselector.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.characterselector.CharacterSelectorScreen;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
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
        super(x, y, width, height, Component.literal("Class Selection Add Button"));
        this.characterSelectorScreen = characterSelectorScreen;
    }

    @Override
    public void onPress() {
        if (characterSelectorScreen.getFirstNewCharacterSlot() == -1) return;

        Models.CharacterSelection.createNewClass();
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.ADD_ICON_OFFSET.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                characterSelectorScreen.getFirstNewCharacterSlot() == -1 ? Texture.ADD_ICON_OFFSET.height() / 2 : 0,
                Texture.ADD_ICON_OFFSET.width(),
                Texture.ADD_ICON_OFFSET.height() / 2,
                Texture.ADD_ICON_OFFSET.width(),
                Texture.ADD_ICON_OFFSET.height());

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
