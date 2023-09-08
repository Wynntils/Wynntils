/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.transcription.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.wynnalphabet.WynnAlphabet;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class WynnAlphabetButton extends WynntilsButton {
    private final WynnAlphabet wynnAlphabet;

    public WynnAlphabetButton(int x, int y, int width, int height, WynnAlphabet wynnAlphabet) {
        super(x, y, width, height, Component.literal("Wynn Alphabet Button"));
        this.wynnAlphabet = wynnAlphabet;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (wynnAlphabet == null) return;

        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRect(
                poseStack, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), 0, width, height);

        CustomColor color = getButtonColor();

        StyledText buttonText;

        switch (wynnAlphabet) {
            case WYNNIC -> buttonText = StyledText.fromString("W");
            case GAVELLIAN -> buttonText = StyledText.fromString("G");
            default -> buttonText = StyledText.fromString("N");
        }

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        buttonText,
                        getX() + 1,
                        getX() + width,
                        getY() + 1,
                        getY() + height,
                        0,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }

    private CustomColor getButtonColor() {
        if (Models.WynnAlphabet.getSelectedAlphabet() == wynnAlphabet) {
            return CommonColors.GREEN;
        } else {
            return CommonColors.WHITE;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        Models.WynnAlphabet.setSelectedAlphabet(wynnAlphabet);

        return false;
    }

    @Override
    public void onPress() {}
}
