/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.settings.WynntilsSettingsScreen;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

// FIXME: This is a very basic text box.
public class TextInputBoxWidget extends AbstractWidget {
    protected char defaultCursorChar = '_';
    protected Consumer<String> onUpdateConsumer;
    protected String textBoxInput = "";
    protected int cursorPosition = 0;
    protected long lastCursorSwitch = 0;
    protected boolean renderCursor = true;

    protected final WynntilsSettingsScreen settingsScreen;

    public TextInputBoxWidget(
            int x,
            int y,
            int width,
            int height,
            Component boxTitle,
            Consumer<String> onUpdateConsumer,
            WynntilsSettingsScreen settingsScreen) {
        super(x, y, width, height, boxTitle);
        this.onUpdateConsumer = onUpdateConsumer;
        this.settingsScreen = settingsScreen;
    }

    public TextInputBoxWidget(
            int x,
            int y,
            int width,
            int height,
            Consumer<String> onUpdateConsumer,
            WynntilsSettingsScreen settingsScreen) {
        super(x, y, width, height, TextComponent.EMPTY);
        this.onUpdateConsumer = onUpdateConsumer;
        this.settingsScreen = settingsScreen;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();

        poseStack.translate(this.x, this.y, 0);

        this.renderBg(poseStack, McUtils.mc(), mouseX, mouseY);

        Font font = FontRenderer.getInstance().getFont();
        final float maxTextWidth = this.width - 4;

        String cursorChar = getRenderCursorChar();

        final float cursorWidth = font.width(String.valueOf(this.getCursorChar()));

        String renderedText;
        if (font.width(textBoxInput + this.getCursorChar()) < maxTextWidth) {
            renderedText =
                    (textBoxInput.substring(0, cursorPosition) + cursorChar + textBoxInput.substring(cursorPosition));
        } else {
            // This case, the input is too long, only render text that fits, and is closest to cursor
            StringBuilder builder = new StringBuilder(cursorChar);

            int stringPosition = Math.min(textBoxInput.length() - 1, cursorPosition);

            while (font.width(builder.toString()) < maxTextWidth - cursorWidth && stringPosition >= 0) {
                builder.append(textBoxInput.charAt(stringPosition));

                stringPosition--;
            }

            builder.reverse();

            stringPosition = cursorPosition + 1;

            while (font.width(builder.toString()) < maxTextWidth - cursorWidth
                    && stringPosition < this.textBoxInput.length()) {
                builder.append(textBoxInput.charAt(stringPosition));

                stringPosition++;
            }

            renderedText = builder.toString();
        }

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        renderedText,
                        2,
                        this.width,
                        2,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        FontRenderer.TextShadow.NORMAL);

        poseStack.popPose();
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
        RenderUtils.drawRect(poseStack, CommonColors.BLACK, 0, 0, 0, this.width, this.height);
        RenderUtils.drawRectBorders(poseStack, CommonColors.WHITE, 0, 0, this.width, this.height, 0, 2);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        McUtils.soundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        settingsScreen.setFocusedTextInput(this);

        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (textBoxInput == null) {
            textBoxInput = "";
        }

        textBoxInput = textBoxInput.substring(0, cursorPosition) + codePoint + textBoxInput.substring(cursorPosition);
        cursorPosition = Math.min(textBoxInput.length(), cursorPosition + 1);
        this.onUpdateConsumer.accept(this.getTextBoxInput());
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            removeFocus();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (textBoxInput.length() == 0) {
                return false;
            }

            textBoxInput =
                    textBoxInput.substring(0, Math.max(0, cursorPosition - 1)) + textBoxInput.substring(cursorPosition);
            cursorPosition = Math.max(0, cursorPosition - 1);
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            cursorPosition = Math.max(0, cursorPosition - 1);
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            cursorPosition = Math.min(textBoxInput.length(), cursorPosition + 1);
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return false;
        }

        return false;
    }

    @Override
    public boolean isFocused() {
        return settingsScreen.getFocusedTextInput() == this;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    protected String getRenderCursorChar() {
        String cursorChar;
        if (System.currentTimeMillis() - lastCursorSwitch > 350) {
            renderCursor = !renderCursor;
            lastCursorSwitch = System.currentTimeMillis();
        }

        cursorChar = isFocused() && renderCursor ? String.valueOf(this.getCursorChar()) : "";
        return cursorChar;
    }

    protected void removeFocus() {
        settingsScreen.setFocusedTextInput(null);
    }

    public void setTextBoxInput(String textBoxInput) {
        this.textBoxInput = textBoxInput;
        this.cursorPosition = this.textBoxInput.length();
        this.onUpdateConsumer.accept(this.textBoxInput);
    }

    public char getCursorChar() {
        return this.defaultCursorChar;
    }

    public String getTextBoxInput() {
        return textBoxInput;
    }
}
