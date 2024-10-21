/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.mythicblacksmith.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class ConfirmWidget extends AbstractWidget {
    private final ConfirmSlider slider;
    private boolean confirmed = false;

    private CustomColor borderColor = CommonColors.RED;
    private CustomColor textColor = CommonColors.WHITE;

    public ConfirmWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Mythic Blacksmith Confirm Slider Widget"));

        this.slider = new ConfirmSlider(x + 1, y + 1, 20, this.height - 2, width);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString(I18n.get("feature.wynntils.mythicBlacksmithWarn.slideToUnlock")),
                        getX() + (float) getWidth() / 2,
                        getY() - 1,
                        textColor,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        RenderUtils.drawRectBorders(
                guiGraphics.pose(), borderColor, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 1, 1);
        slider.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        if (slider.isSliding || confirmed) return;

        slider.decaySlider();
        borderColor = CommonColors.RED;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        slider.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        slider.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        if (slider.sliderValue >= 0.99F) {
            confirmed = true;
            borderColor = CommonColors.GREEN;
        } else {
            confirmed = false;
            borderColor = CommonColors.RED;
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        slider.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setTextColor(CustomColor color) {
        this.textColor = color;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
