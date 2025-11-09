/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.secrets.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.MaskedTextInputWidget;
import com.wynntils.services.secrets.type.SecretKey;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class SecretInputWidget extends AbstractWidget {
    private final MaskedTextInputWidget maskedTextInputWidget;
    private final SecretKey secretKey;

    public SecretInputWidget(int x, int y, int width, int height, TextboxScreen textboxScreen, SecretKey secretKey) {
        super(x, y, width, height, null);

        this.maskedTextInputWidget = new MaskedTextInputWidget(
                x + 80, y, width - 80, height, (s) -> Services.Secrets.setSecret(secretKey, s), textboxScreen);
        this.secretKey = secretKey;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics.pose(),
                        StyledText.fromString(EnumUtils.toNiceString(secretKey)),
                        getX(),
                        getY() + getHeight() / 2f,
                        80,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        maskedTextInputWidget.render(guiGraphics, mouseX, mouseY, partialTick);

        if (isHovered) {
            McUtils.screen().setTooltipForNextRenderPass(Component.translatable(secretKey.getDescriptionKey()));
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        maskedTextInputWidget.setY(y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (maskedTextInputWidget.isMouseOver(mouseX, mouseY)) {
            return maskedTextInputWidget.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (maskedTextInputWidget.isMouseOver(mouseX, mouseY)) {
            return maskedTextInputWidget.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (maskedTextInputWidget.isMouseOver(mouseX, mouseY)) {
            return maskedTextInputWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
