/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TexturedTextInputBoxWidget extends TextInputBoxWidget {
    private static final Component DEFAULT_TEXT =
            Component.translatable("screens.wynntils.map.managers.categoryManager.TexturedTextInputBoxWidget.default");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d*$");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("^-?\\d*\\.?\\d*$");
    private static final Pattern HEXSTRING_PATTERN = Pattern.compile("^#?[0-9a-fA-F]{0,8}$");
    // Accepted Identifier name pattern `^[a-z0-9/._-]+`
    // This is made stricter to avoid weird names
    private static final Pattern STRICT_IDENTIFIER_PATTERN = Pattern.compile("^[a-z0-9-]*$");

    private final Mode mode;

    public TexturedTextInputBoxWidget(
            int x,
            int y,
            int width,
            int height,
            Consumer<String> onUpdateConsumer,
            TextboxScreen textboxScreen,
            Mode mode) {
        super(
                x,
                y,
                width,
                height,
                Component.literal("Textured Text Input Box Widget"),
                wrapConsumer(mode, onUpdateConsumer),
                textboxScreen);
        this.mode = mode;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        String oldText = getTextBoxInput();
        int oldCursor = cursorPosition;

        boolean result = super.charTyped(event);

        enforceValidInput(oldText, oldCursor, event.codepoint());

        return result;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        String oldText = getTextBoxInput();
        int oldCursor = cursorPosition;

        boolean result = super.keyPressed(event);

        enforceValidInput(oldText, oldCursor, event.key());

        return result;
    }

    @Override
    public void setTextBoxInput(String textBoxInput) {
        if (isInputValid(textBoxInput)) {
            super.setTextBoxInput(textBoxInput);
        }
    }

    private void enforceValidInput(String oldText, int oldCursor, int keyCode) {
        if (!isInputValid(getTextBoxInput())) {
            if (mode == Mode.IDENTIFIER && keyCode == GLFW.GLFW_KEY_SPACE) {
                super.setTextBoxInput(getTextBoxInput().replaceAll(" ", "-"));
                setCursorAndHighlightPositions(oldCursor + 1);
                return;
            }

            super.setTextBoxInput(oldText);
            setCursorAndHighlightPositions(oldCursor);
        }
    }

    private boolean isInputValid(String input) {
        return isInputValid(mode, input);
    }

    private static boolean isInputValid(Mode mode, String input) {
        return switch (mode) {
            case STRING -> true;
            case INTEGER -> INTEGER_PATTERN.matcher(input).matches();
            case FLOAT -> FLOAT_PATTERN.matcher(input).matches();
            case HEXSTRING -> HEXSTRING_PATTERN.matcher(input).matches();
            case IDENTIFIER -> STRICT_IDENTIFIER_PATTERN.matcher(input).matches();
        };
    }

    private static Consumer<String> wrapConsumer(Mode mode, Consumer<String> onUpdateConsumer) {
        return text -> {
            if (isInputValid(mode, text)) {
                onUpdateConsumer.accept(text);
            }
        };
    }

    public Integer getValueAsInt() {
        try {
            return Integer.parseInt(getTextBoxInput());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Float getValueAsFloat() {
        try {
            return Float.parseFloat(getTextBoxInput());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected void doRenderWidget(
            GuiGraphics guiGraphics,
            String renderedText,
            int renderedTextStart,
            String firstPortion,
            String highlightedPortion,
            String lastPortion,
            Font font,
            int firstWidth,
            int highlightedWidth,
            int lastWidth,
            int mouseX,
            int mouseY) {
        if (this.isHovered) {
            guiGraphics.requestCursor(CursorTypes.IBEAM);
        }

        guiGraphics.pose().pushMatrix();

        guiGraphics.pose().translate(this.getX(), this.getY());

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, 0, 0, this.width, this.height);

        boolean defaultText = Objects.equals(textBoxInput, "");

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(defaultText ? DEFAULT_TEXT.getString() : firstPortion),
                        textPadding,
                        this.width - lastWidth - highlightedWidth,
                        textPadding,
                        this.height - textPadding,
                        0,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (!defaultText) {
            FontRenderer.getInstance()
                    .renderAlignedHighlightedTextInBox(
                            guiGraphics,
                            StyledText.fromString(highlightedPortion),
                            textPadding + firstWidth,
                            this.width - lastWidth,
                            textPadding,
                            this.height - textPadding,
                            0,
                            CommonColors.BLUE,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromString(lastPortion),
                            textPadding + firstWidth + highlightedWidth,
                            this.width,
                            textPadding,
                            this.height - textPadding,
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        drawCursor(
                guiGraphics,
                font.width(renderedText.substring(0, Math.min(cursorPosition, renderedText.length()))),
                (textPadding + this.height - textPadding) / 2f,
                VerticalAlignment.MIDDLE,
                false);

        if (isHovered && tooltip != null) {
            guiGraphics.setTooltipForNextFrame(Lists.transform(tooltip, Component::getVisualOrderText), mouseX, mouseY);
        }

        guiGraphics.pose().popMatrix();
    }

    public enum Mode {
        STRING,
        INTEGER,
        FLOAT,
        HEXSTRING,
        IDENTIFIER
    }
}
