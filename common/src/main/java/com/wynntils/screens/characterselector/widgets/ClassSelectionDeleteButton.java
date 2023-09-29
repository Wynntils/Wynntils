/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.characterselector.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.characterselector.CharacterSelectorScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ClassSelectionDeleteButton extends WynntilsButton {
    private static final List<Component> TOOLTIP = List.of(
            Component.translatable("screens.wynntils.characterSelection.delete.name")
                    .withStyle(ChatFormatting.RED),
            Component.translatable("screens.wynntils.characterSelection.delete.discussion")
                    .withStyle(ChatFormatting.GRAY));
    private final CharacterSelectorScreen characterSelectorScreen;

    public ClassSelectionDeleteButton(
            int x, int y, int width, int height, CharacterSelectorScreen characterSelectorScreen) {
        super(x, y, width, height, Component.literal("Class Selection Delete Button"));
        this.characterSelectorScreen = characterSelectorScreen;
    }

    @Override
    public void onPress() {
        if (characterSelectorScreen.getSelected() == null) return;

        Models.CharacterSelection.deleteCharacter(
                characterSelectorScreen.getSelected().getClassInfo().slot());
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.REMOVE_ICON_OFFSET.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                characterSelectorScreen.getSelected() == null ? Texture.REMOVE_ICON_OFFSET.height() / 2 : 0,
                Texture.REMOVE_ICON_OFFSET.width(),
                Texture.REMOVE_ICON_OFFSET.height() / 2,
                Texture.REMOVE_ICON_OFFSET.width(),
                Texture.REMOVE_ICON_OFFSET.height());

        if (isHovered && characterSelectorScreen.getSelected() != null) {
            McUtils.mc().screen.setTooltipForNextRenderPass(Lists.transform(TOOLTIP, Component::getVisualOrderText));
        }
    }
}
