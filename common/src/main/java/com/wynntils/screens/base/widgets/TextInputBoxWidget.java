/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.CodedString;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class TextInputBoxWidget extends AbstractWidget {
    private static final int CURSOR_PADDING = 3;
    private static final int CURSOR_TICK = 350;

    private final Consumer<String> onUpdateConsumer;
    protected String textBoxInput = "";
    protected int cursorPosition = 0;
    protected int highlightPosition = 0;
    private long lastCursorSwitch = 0;
    protected boolean renderCursor = true;
    private CustomColor renderColor = CommonColors.WHITE;

    protected boolean isDragging = false;

    protected final TextboxScreen textboxScreen;
    protected int textPadding = 2;

    protected TextInputBoxWidget(
            int x,
            int y,
            int width,
            int height,
            Component boxTitle,
            Consumer<String> onUpdateConsumer,
            TextboxScreen textboxScreen) {
        super(x, y, width, height, boxTitle);
        this.onUpdateConsumer = onUpdateConsumer == null ? s -> {} : onUpdateConsumer;
        this.textboxScreen = textboxScreen;
    }

    public TextInputBoxWidget(
            int x,
            int y,
            int width,
            int height,
            Consumer<String> onUpdateConsumer,
            TextboxScreen textboxScreen,
            TextInputBoxWidget oldWidget) {
        this(x, y, width, height, Component.empty(), onUpdateConsumer, textboxScreen);

        if (oldWidget != null) {
            this.textBoxInput = oldWidget.textBoxInput;
            setCursorAndHighlightPositions(oldWidget.cursorPosition);
            this.renderColor = oldWidget.renderColor;
        }
    }

    public TextInputBoxWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        this(x, y, width, height, onUpdateConsumer, textboxScreen, null);
    }

    @Override
    public final void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderWidget(poseStack, mouseX, mouseY, partialTick);
    }

    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Pair<String, Integer> renderedTextDetails = getRenderedText(getMaxTextWidth());
        String renderedText = renderedTextDetails.a();

        Pair<Integer, Integer> highlightedVisibleInterval = getRenderedHighlighedInterval(renderedText);

        int startIndex = highlightedVisibleInterval.a();
        int endIndex = highlightedVisibleInterval.b();

        String firstPortion = renderedText.substring(0, startIndex);
        String highlightedPortion = renderedText.substring(startIndex, endIndex);
        String lastPortion = renderedText.substring(endIndex);

        Font font = FontRenderer.getInstance().getFont();

        int firstWidth = font.width(firstPortion);
        int highlightedWidth = font.width(highlightedPortion);
        int lastWidth = font.width(lastPortion);

        doRenderWidget(
                poseStack,
                renderedText,
                firstPortion,
                highlightedPortion,
                lastPortion,
                font,
                firstWidth,
                highlightedWidth,
                lastWidth);
    }

    protected void doRenderWidget(
            PoseStack poseStack,
            String renderedText,
            String firstPortion,
            String highlightedPortion,
            String lastPortion,
            Font font,
            int firstWidth,
            int highlightedWidth,
            int lastWidth) {
        poseStack.pushPose();

        poseStack.translate(this.getX(), this.getY(), 0);

        RenderUtils.drawRect(poseStack, CommonColors.BLACK, 0, 0, 1, this.width, this.height);
        RenderUtils.drawRectBorders(poseStack, CommonColors.GRAY, 0, 0, this.width, this.height, 1, 2);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        CodedString.of(firstPortion),
                        textPadding,
                        this.width - lastWidth - highlightedWidth,
                        textPadding,
                        this.height - textPadding,
                        0,
                        renderColor,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderAlignedHighlightedTextInBox(
                        poseStack,
                        CodedString.of(highlightedPortion),
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
                        poseStack,
                        CodedString.of(lastPortion),
                        textPadding + firstWidth + highlightedWidth,
                        this.width,
                        textPadding,
                        this.height - textPadding,
                        0,
                        renderColor,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        drawCursor(
                poseStack,
                font.width(renderedText.substring(0, Math.min(cursorPosition, renderedText.length()))),
                (textPadding + this.height - textPadding) / 2,
                VerticalAlignment.MIDDLE,
                false);

        poseStack.popPose();
    }

    protected int getMaxTextWidth() {
        return this.width - 8;
    }

    /**
     * Determines the text to render based on cursor position and maxTextWidth
     * @return The text to render, and the starting position of the text within the entire text
     */
    protected Pair<String, Integer> getRenderedText(float maxTextWidth) {
        Font font = FontRenderer.getInstance().getFont();

        if (font.width(textBoxInput) < maxTextWidth) {
            return Pair.of(textBoxInput, 0);
        }

        StringBuilder builder = new StringBuilder();

        // First append to the left of the cursor
        int stringPosition = cursorPosition - 1;
        while (font.width(builder.toString()) < maxTextWidth && stringPosition >= 0) {
            builder.append(textBoxInput.charAt(stringPosition));

            stringPosition--;
        }

        final int startingAt = Math.max(stringPosition, 0); // If we went too far, start at the beginning

        // Now reverse so it's actually to the left
        builder.reverse();

        // Now append to the right of the cursor
        stringPosition = cursorPosition;
        while (font.width(builder.toString()) < maxTextWidth && stringPosition < this.textBoxInput.length()) {
            builder.append(textBoxInput.charAt(stringPosition));

            stringPosition++;
        }

        return Pair.of(builder.toString(), startingAt);
    }

    /**
     * @return A Pair of numbers representing the start and end of the highlight, relative to the rendered text.
     * This interval is zero indexed.
     * This does NOT represent the *entire* highlighted portion, just the VISIBLE part!
     */
    protected Pair<Integer, Integer> getRenderedHighlighedInterval(String renderedText) {
        if (renderedText.isEmpty()) {
            return Pair.of(0, 0);
        }

        int length = renderedText.length();

        int highlightedStart = Math.min(cursorPosition, highlightPosition);
        int highlightedEnd = Math.max(cursorPosition, highlightPosition);

        Pair<Integer, Integer> renderedInterval = Pair.of(0, length);
        Pair<Integer, Integer> highlightedInterval = Pair.of(highlightedStart, highlightedEnd);
        int a = 0;
        int b = 0;

        // get intersection of renderedInterval and highlightedInterval
        if (highlightedInterval.a() <= renderedInterval.b() && renderedInterval.a() <= highlightedInterval.b()) {
            a = Math.max(renderedInterval.a(), highlightedInterval.a());
            b = Math.min(renderedInterval.b(), highlightedInterval.b());
        }

        a = Mth.clamp(a, 0, length);
        b = Mth.clamp(b, 0, length);

        return Pair.of(a, b);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        McUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());

        if (this.isHovered) {
            setCursorAndHighlightPositions(getIndexAtPosition(mouseX));
            isDragging = true;
            textboxScreen.setFocusedTextInput(this);
            this.setFocused(true);
            return true;
        } else {
            setCursorAndHighlightPositions(cursorPosition); // remove highlights when clicking off
            this.setFocused(false);
            textboxScreen.setFocusedTextInput(null);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            setCursorPosition(getIndexAtPosition(mouseX));
        }

        return true;
    }

    protected int getIndexAtPosition(double mouseX) {
        // mouseX is actually just the x position of the mouse relative to the screen, not the textbox
        mouseX -= this.getX();
        mouseX -= textPadding; // Account for padding

        Pair<String, Integer> renderedTextDetails = getRenderedText(getMaxTextWidth());
        String renderedText = renderedTextDetails.a();
        int shift = renderedTextDetails.b();

        Font font = FontRenderer.getInstance().getFont();

        if (font.width(renderedText) < mouseX) {
            return renderedText.length() + shift;
        }

        int closestWidthCharIndex = 0;
        double closestDistance = Double.MAX_VALUE;
        for (int i = 0; i < renderedText.length(); i++) {
            float width = font.width(renderedText.substring(0, i));
            double distance = Math.abs(mouseX - width);

            // If distance starts increasing, we've gone too far
            if (distance > closestDistance) break;

            closestDistance = distance;
            closestWidthCharIndex = i;
        }

        return closestWidthCharIndex + shift;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (textBoxInput == null) {
            textBoxInput = "";
        }

        if (hasHighlighted()) {
            replaceHighlighted(String.valueOf(codePoint));
        } else {
            textBoxInput =
                    textBoxInput.substring(0, cursorPosition) + codePoint + textBoxInput.substring(cursorPosition);
            setCursorPosition(cursorPosition + 1);
            setHighlightPosition(cursorPosition);
        }

        this.onUpdateConsumer.accept(this.getTextBoxInput());

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            removeFocus();
            return true;
        }

        if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance()
                    .keyboardHandler
                    .setClipboard(hasHighlighted() ? getHighlightedText() : getTextBoxInput());
            return true;
        } else if (Screen.isPaste(keyCode)) {
            if (hasHighlighted()) {
                replaceHighlighted(Minecraft.getInstance().keyboardHandler.getClipboard());
            } else {
                this.setTextBoxInput((textBoxInput.substring(0, cursorPosition)
                        + Minecraft.getInstance().keyboardHandler.getClipboard()
                        + textBoxInput.substring(cursorPosition)));
            }

            return true;
        } else if (Screen.isCut(keyCode)) {
            if (hasHighlighted()) {
                Minecraft.getInstance().keyboardHandler.setClipboard(getHighlightedText());
                replaceHighlighted("");
            } else {
                Minecraft.getInstance().keyboardHandler.setClipboard(getTextBoxInput());
                setTextBoxInput("");
            }

            return true;
        } else if (Screen.isSelectAll(keyCode)) {
            setCursorPosition(textBoxInput.length());
            setHighlightPosition(0);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (textBoxInput.isEmpty()) {
                return true;
            }

            if (this.hasHighlighted()) {
                replaceHighlighted("");
                return true;
            }

            if (Screen.hasControlDown()) {
                setTextBoxInput(textBoxInput.substring(cursorPosition));
                setCursorAndHighlightPositions(0);
                return true;
            }

            textBoxInput =
                    textBoxInput.substring(0, Math.max(0, cursorPosition - 1)) + textBoxInput.substring(cursorPosition);
            setCursorAndHighlightPositions(cursorPosition - 1);
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (textBoxInput.isEmpty()) {
                return true;
            }

            if (this.hasHighlighted()) {
                replaceHighlighted("");
                return true;
            }

            if (Screen.hasControlDown()) {
                setTextBoxInput(textBoxInput.substring(0, cursorPosition));
                return true;
            }

            textBoxInput = textBoxInput.substring(0, cursorPosition)
                    + textBoxInput.substring(Math.min(textBoxInput.length(), cursorPosition + 1));
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (hasHighlighted() && !Screen.hasShiftDown() && !Screen.hasControlDown()) {
                setCursorAndHighlightPositions(Math.min(cursorPosition, highlightPosition));
                return true;
            }

            if (Screen.hasControlDown() && Screen.hasShiftDown()) {
                // this should move the cursor all the way left and highlight everything
                setCursorPosition(0);
                return true;
            } else if (Screen.hasControlDown()) {
                // this should move cursor all the way left and not highlight anything
                setCursorAndHighlightPositions(0);
                return true;
            } else if (Screen.hasShiftDown()) {
                // this should move the cursor left and highlight the text
                setCursorPosition(cursorPosition - 1);
                return true;
            } else if (hasHighlighted()) {
                setCursorAndHighlightPositions(Math.min(cursorPosition, highlightPosition));
                return true;
            }
            // this should move the cursor left and not highlight anything
            setCursorAndHighlightPositions(cursorPosition - 1);
            return true; // no need to call onUpdateConsumer here because we aren't changing the text
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (Screen.hasControlDown() && Screen.hasShiftDown()) {
                // this should move the cursor all the way right and highlight everything
                setCursorPosition(textBoxInput.length());
                return true;
            } else if (Screen.hasControlDown()) {
                // this should move cursor all the way right and not highlight anything
                setCursorAndHighlightPositions(textBoxInput.length());
                return true;
            } else if (Screen.hasShiftDown()) {
                // this should move the cursor right and highlight the text
                setCursorPosition(cursorPosition + 1);
                return true;
            } else if (hasHighlighted()) {
                setCursorAndHighlightPositions(Math.max(cursorPosition, highlightPosition));
                return true;
            }
            // this should move the cursor right and not highlight anything
            setCursorAndHighlightPositions(cursorPosition + 1);
            return true; // no need to call onUpdateConsumer here because we aren't changing the text
        }

        if (keyCode == GLFW.GLFW_KEY_HOME) {
            setCursorAndHighlightPositions(0);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_END) {
            setCursorAndHighlightPositions(textBoxInput.length());
            return true;
        }

        return true;
    }

    @Override
    public boolean isFocused() {
        return textboxScreen.getFocusedTextInput() == this;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    protected void drawCursor(
            PoseStack poseStack, float x, float y, VerticalAlignment verticalAlignment, boolean forceUnfocusedCursor) {
        if (isDragging || hasHighlighted()) return;

        if (System.currentTimeMillis() - lastCursorSwitch > CURSOR_TICK) {
            renderCursor = !renderCursor;
            lastCursorSwitch = System.currentTimeMillis();
        }

        if (!renderCursor) return;

        if (isFocused() || forceUnfocusedCursor) {
            Font font = FontRenderer.getInstance().getFont();

            float cursorRenderY =
                    switch (verticalAlignment) {
                        case TOP -> y - (CURSOR_PADDING - 1);
                        case MIDDLE -> y - font.lineHeight + (CURSOR_PADDING - 1);
                        case BOTTOM -> y - font.lineHeight - (CURSOR_PADDING - 1);
                    };

            RenderUtils.drawRect(poseStack, CommonColors.WHITE, x + 1, cursorRenderY, 0, 1, font.lineHeight + 3);
        }
    }

    protected void removeFocus() {
        textboxScreen.setFocusedTextInput(null);
    }

    public void setTextBoxInput(String textBoxInput) {
        this.textBoxInput = textBoxInput;
        setCursorAndHighlightPositions(textBoxInput.length());

        this.onUpdateConsumer.accept(this.textBoxInput);
    }

    /**
     * Sets the cursor position to the given value.
     * Accepts values outside the bounds of the text box, it will clamp them.
     * @param cursorPosition
     */
    public void setCursorPosition(int cursorPosition) {
        this.cursorPosition = MathUtils.clamp(cursorPosition, 0, textBoxInput.length());
    }

    /**
     * Sets the cursor position and the highlight position to the given value.
     * This means there will be no highlight.
     * Accepts values outside the bounds of the text box, it will clamp them.
     */
    public void setCursorAndHighlightPositions(int pos) {
        this.cursorPosition = MathUtils.clamp(pos, 0, textBoxInput.length());
        this.highlightPosition = this.cursorPosition;
    }

    public String getTextBoxInput() {
        return textBoxInput;
    }

    public String getHighlightedText() {
        int startIndex = Math.min(this.cursorPosition, this.highlightPosition);
        int endIndex = Math.max(this.cursorPosition, this.highlightPosition);

        return this.textBoxInput.substring(startIndex, endIndex);
    }

    public void setRenderColor(CustomColor renderColor) {
        this.renderColor = renderColor;
    }

    public boolean hasHighlighted() {
        return this.cursorPosition != this.highlightPosition;
    }

    public void setHighlightPosition(int position) {
        int length = this.textBoxInput.length();
        this.highlightPosition = Mth.clamp(position, 0, length);
    }

    public void replaceHighlighted(String text) {
        int startIndex = Math.min(this.cursorPosition, this.highlightPosition);
        int endIndex = Math.max(this.cursorPosition, this.highlightPosition);

        int insertLength = text.length();

        this.textBoxInput = new StringBuilder(this.textBoxInput)
                .replace(startIndex, endIndex, text)
                .toString();
        this.setCursorPosition(startIndex + insertLength);
        this.setHighlightPosition(this.cursorPosition);
        this.onUpdateConsumer.accept(this.textBoxInput);
    }
}
