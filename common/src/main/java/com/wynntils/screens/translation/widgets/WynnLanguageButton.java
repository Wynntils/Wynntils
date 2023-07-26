/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.translation.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.translation.WynnLanguage;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.network.chat.Component;

public class WynnLanguageButton extends WynntilsButton {
    private final WynnLanguage wynnLanguage;

    public WynnLanguageButton(int x, int y, int width, int height, WynnLanguage wynnLanguage) {
        super(x, y, width, height, Component.literal("Wynn Language Button"));
        this.wynnLanguage = wynnLanguage;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (wynnLanguage == null) return;

        RenderUtils.drawRect(
                poseStack, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), 0, width, height);

        CustomColor color = getButtonColor();

        StyledText buttonText;

        switch (wynnLanguage) {
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
        if (Services.WynnLanguageSerivce.getSelectedLanguage() == wynnLanguage) {
            return CommonColors.GREEN;
        } else {
            return CommonColors.WHITE;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        Services.WynnLanguageSerivce.setSelectedLanguage(wynnLanguage);

        return false;
    }

    @Override
    public void onPress() {}
}
