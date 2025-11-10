/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TextboxScreen;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class MaskedTextInputWidget extends AbstractWidget {
    private boolean masked = true;
    private final MaskedTextInputBoxWidget maskedTextInputBoxWidget;
    private final Button toggleMaskButton;

    public MaskedTextInputWidget(
            int x,
            int y,
            int width,
            int height,
            Consumer<String> onUpdateConsumer,
            TextboxScreen textboxScreen,
            String initialText) {
        super(x, y, width, height, Component.literal("Masked Text Input"));

        this.maskedTextInputBoxWidget =
                new MaskedTextInputBoxWidget(x, y, width - 42, height, onUpdateConsumer, textboxScreen);
        this.maskedTextInputBoxWidget.setTextBoxInput(initialText);

        this.toggleMaskButton = new Button.Builder(
                        masked
                                ? Component.translatable("screens.wynntils.widget.show")
                                : Component.translatable("screens.wynntils.widget.hide"),
                        (button -> toggleMask()))
                .pos(x + width - 40, y)
                .size(40, 20)
                .build();
    }

    public MaskedTextInputWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        this(x, y, width, height, onUpdateConsumer, textboxScreen, "");
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        maskedTextInputBoxWidget.render(guiGraphics, mouseX, mouseY, partialTick);

        toggleMaskButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (toggleMaskButton.isMouseOver(mouseX, mouseY)) {
            return toggleMaskButton.mouseClicked(mouseX, mouseY, button);
        } else if (maskedTextInputBoxWidget.isMouseOver(mouseX, mouseY)) {
            return maskedTextInputBoxWidget.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (maskedTextInputBoxWidget.isMouseOver(mouseX, mouseY)) {
            return maskedTextInputBoxWidget.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (maskedTextInputBoxWidget.isMouseOver(mouseX, mouseY)) {
            return maskedTextInputBoxWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        maskedTextInputBoxWidget.setY(y);
        toggleMaskButton.setY(y);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private void toggleMask() {
        masked = !masked;
        toggleMaskButton.setMessage(
                masked
                        ? Component.translatable("screens.wynntils.widget.show")
                        : Component.translatable("screens.wynntils.widget.hide"));
    }

    private final class MaskedTextInputBoxWidget extends TextInputBoxWidget {
        private MaskedTextInputBoxWidget(
                int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
            super(x, y, width, height, onUpdateConsumer, textboxScreen, null);
        }

        @Override
        protected void doRenderWidget(
                PoseStack poseStack,
                String renderedText,
                int renderedTextStart,
                String firstPortion,
                String highlightedPortion,
                String lastPortion,
                Font font,
                int firstWidth,
                int highlightedWidth,
                int lastWidth) {
            super.doRenderWidget(
                    poseStack,
                    masked ? "*".repeat(renderedText.length()) : renderedText,
                    renderedTextStart,
                    masked ? "*".repeat(firstPortion.length()) : firstPortion,
                    masked ? "*".repeat(highlightedPortion.length()) : highlightedPortion,
                    masked ? "*".repeat(lastPortion.length()) : lastPortion,
                    font,
                    firstWidth,
                    highlightedWidth,
                    lastWidth);
        }
    }
}
