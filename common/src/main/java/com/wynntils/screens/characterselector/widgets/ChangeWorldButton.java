/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.characterselector.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.characterselector.CharacterSelectorScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class ChangeWorldButton extends WynntilsButton {
    private static final List<Component> TOOLTIP = List.of(
            Component.translatable("screens.wynntils.characterSelection.changeWorldButton.changeWorld")
                    .withStyle(ChatFormatting.GREEN),
            Component.translatable("screens.wynntils.characterSelection.changeWorldButton.description")
                    .withStyle(ChatFormatting.GRAY));
    private final CharacterSelectorScreen characterSelectorScreen;

    public ChangeWorldButton(int x, int y, int width, int height, CharacterSelectorScreen characterSelectorScreen) {
        super(x, y, width, height, Component.literal("Change World"));
        this.characterSelectorScreen = characterSelectorScreen;
    }

    @Override
    public void onPress() {
        McUtils.sendCommand("hub");
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.CHANGE_WORLD_BUTTON.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                isHovered ? 0 : Texture.CHANGE_WORLD_BUTTON.height() / 2,
                Texture.CHANGE_WORLD_BUTTON.width(),
                Texture.CHANGE_WORLD_BUTTON.height() / 2,
                Texture.CHANGE_WORLD_BUTTON.width(),
                Texture.CHANGE_WORLD_BUTTON.height());

        if (isHovered) {
            poseStack.pushPose();
            List<ClientTooltipComponent> clientTooltipComponents =
                    TooltipUtils.componentToClientTooltipComponent(TOOLTIP);
            poseStack.translate(
                    mouseX
                            - TooltipUtils.getToolTipWidth(
                                    clientTooltipComponents,
                                    FontRenderer.getInstance().getFont()),
                    mouseY - TooltipUtils.getToolTipHeight(clientTooltipComponents),
                    100);
            RenderUtils.drawTooltip(
                    poseStack, TOOLTIP, FontRenderer.getInstance().getFont(), true);
            poseStack.popPose();
        }
    }
}
