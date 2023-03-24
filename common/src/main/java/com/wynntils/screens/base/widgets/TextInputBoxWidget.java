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
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.wynntils.utils.type.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

// FIXME: Add selection support to this class to be a fully working text box
public class TextInputBoxWidget extends AbstractWidget {
    private static final char DEFAULT_CURSOR_CHAR = '|';
    private final Consumer<String> onUpdateConsumer;
    protected String textBoxInput = "";
    protected int cursorPosition = 0;
    protected int highlightPosition = 0;
    private long lastCursorSwitch = 0;
    protected boolean renderCursor = true;
    private CustomColor renderColor = CommonColors.WHITE;
    protected boolean isDragging = false;

    protected final TextboxScreen textboxScreen;
    private final int maxTextWidth = this.width - 8;
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
            setCursorPosition(oldWidget.cursorPosition);
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
        poseStack.pushPose();

        poseStack.translate(this.getX(), this.getY(), 0);

        RenderUtils.drawRect(poseStack, CommonColors.BLACK, 0, 0, 0, this.width, this.height);
        RenderUtils.drawRectBorders(poseStack, CommonColors.GRAY, 0, 0, this.width, this.height, 0, 2);

        Pair<String, Integer> renderedTextDetails = getRenderedText(maxTextWidth, false);
        String renderedText = renderedTextDetails.a();

        Pair<Integer, Integer> highlightedOutputInterval = getRenderedHighlighedInterval(renderedText, renderedTextDetails.b());

        String firstPortion = renderedText.substring(0, highlightedOutputInterval.a());
        String highlightedPortion = renderedText.substring(highlightedOutputInterval.a(), highlightedOutputInterval.b());
        String lastPortion = renderedText.substring(highlightedOutputInterval.b());

        int firstWidth = FontRenderer.getInstance().getFont().width(firstPortion);
        int highlightedWidth = FontRenderer.getInstance().getFont().width(highlightedPortion);
        int lastWidth = FontRenderer.getInstance().getFont().width(lastPortion);

        FontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
                                firstPortion,
                                textPadding,
                                this.width - lastWidth - highlightedWidth,
                                textPadding,
                                this.height - textPadding,
                                0,
                                renderColor,
                                HorizontalAlignment.Left,
                                VerticalAlignment.Middle,
                                TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderAlignedHighlightedTextInBox(
                        poseStack,
                        highlightedPortion,
                        textPadding + firstWidth,
                        this.width - lastWidth,
                        textPadding,
                        this.height - textPadding,
                        0,
                        CommonColors.BLUE,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        lastPortion,
                        textPadding + firstWidth + highlightedWidth,
                        this.width,
                        textPadding,
                        this.height - textPadding,
                        0,
                        renderColor,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);


        poseStack.popPose();
    }

    /**
     * Determines the text to render based on cursor position and maxTextWidth
     * @return The text to render, and the starting position of the text within the entire text
     */
    protected Pair<String, Integer> getRenderedText(float maxTextWidth, boolean forceUnfocusedCursor) {
        Font font = FontRenderer.getInstance().getFont();

        String cursorChar = getRenderCursorChar(forceUnfocusedCursor);
        final int cursorWidth = font.width(cursorChar);

        String entireText = textBoxInput.substring(0, cursorPosition) + cursorChar + textBoxInput.substring(cursorPosition);
        if (font.width(entireText) < maxTextWidth) {
            return Pair.of(entireText, 0);
        }

        StringBuilder builder = new StringBuilder();

        // First append to the left of the cursor
        int stringPosition = cursorPosition - 1;
        while (font.width(builder.toString()) < maxTextWidth - cursorWidth && stringPosition >= 0) {
            builder.append(textBoxInput.charAt(stringPosition));

            stringPosition--;
        }
        int startingAt = stringPosition;

        // Now reverse so it's actually to the left
        builder.reverse();
        builder.append(cursorChar);

        // Now append to the right of the cursor
        stringPosition = cursorPosition;
        while (font.width(builder.toString()) < maxTextWidth - cursorWidth
                && stringPosition < this.textBoxInput.length()) {
            builder.append(textBoxInput.charAt(stringPosition));

            stringPosition++;
        }
        return Pair.of(builder.toString(), startingAt);
    }

    protected Pair<Integer, Integer> getRenderedHighlighedInterval(String renderedText, int shift) {
        int length = renderedText.length();

        int highlightedStart = Math.min(cursorPosition, highlightPosition);
        int highlightedEnd = Math.max(cursorPosition, highlightPosition);

        Pair<Integer, Integer> renderedInterval = Pair.of(0, length);
        Pair<Integer, Integer> highlightedInterval = Pair.of(highlightedStart, highlightedEnd);
        Pair<Integer, Integer> highlightedOutputInterval;

        // get intersection of renderedInterval and highlightedInterval
        if (highlightedInterval.a() > renderedInterval.b() || renderedInterval.a() > highlightedInterval.b()) {
            highlightedOutputInterval = Pair.of(0, 0);
        } else {
            highlightedOutputInterval = Pair.of(Math.max(renderedInterval.a(), highlightedInterval.a()) + shift,
                    Math.min(renderedInterval.b(), highlightedInterval.b()) + shift);
        }

        if (cursorPosition < highlightPosition && renderCursor) {
            // when dragging from right to left, the cursor ends up in the highlight
            // avoid this by moving highlight right
            highlightedOutputInterval = Pair.of(highlightedOutputInterval.a() + 1, highlightedOutputInterval.b() + 1);
        }

        if (highlightedOutputInterval.a() < 0) {
            highlightedOutputInterval = Pair.of(0, highlightedOutputInterval.b());
        }
        if (highlightedOutputInterval.b() > length) {
            highlightedOutputInterval = Pair.of(highlightedOutputInterval.a(), length);
        }
        return highlightedOutputInterval;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        McUtils.playSound(SoundEvents.UI_BUTTON_CLICK.value());

        if (this.isHovered) {
            setCursorAndHighlightPositions(getIndexAtPosition(mouseX));
            isDragging = true;
            textboxScreen.setFocusedTextInput(this);
            return true;
        } else {
            setCursorAndHighlightPositions(cursorPosition); // remove highlights when clicking off
            textboxScreen.setFocusedTextInput(null);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging) {
            isDragging = false;
            setCursorPosition(getIndexAtPosition(mouseX));
        }

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
        mouseX -= this.getX(); // mouseX is actually just the x position of the mouse relative to the screen, not the textbox
        Font font = FontRenderer.getInstance().getFont();
        Pair<String, Integer> renderedTextDetails = getRenderedText(maxTextWidth, false);
        String renderedText = renderedTextDetails.a();
        int startingIndex = renderedTextDetails.b();

        // Width so far at index i
        List<Float> widths = new ArrayList<>();
        for (int i = 0; i < renderedText.length(); i++) {
            // we are using stringWidth here because we need precision; if we use width, it will round to the nearest
            // integer, which will cause strange behaviour when clicking on letters
            widths.add(font.getSplitter().stringWidth(renderedText.substring(0, i)));
        }

        // get nearest width that's in the map
        // FIXME: this is probably really slow and bad, but I am just a first year cs student and I have not taken
        // data structures & algorithms yet so I don't know how to do this better
        mouseX -= textPadding; // Account for padding
        if (mouseX > font.getSplitter().stringWidth(renderedText)) { // Mouse is past the end of the text, return the end of the text
            return startingIndex + renderedText.length();
        } else if (mouseX < 0) { // Mouse is before the start of the text, return the start of the text
            return startingIndex;
        }

        int closestWidthCharIndex = 0;
        double closestWidth = 999999; // Arbitrary large number, there is no way the text will be this wide
        for (float stringWidthSoFar : widths) {
            double widthDiff = Math.abs(stringWidthSoFar - mouseX);
            if (widthDiff < closestWidth) {
                closestWidth = widthDiff;
                closestWidthCharIndex = widths.indexOf(stringWidthSoFar);
            }
        }
        return closestWidthCharIndex + startingIndex;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (textBoxInput == null) {
            textBoxInput = "";
        }

        if (hasHighlighted()) {
            replaceHighlighted(String.valueOf(codePoint));
        } else {
            textBoxInput = textBoxInput.substring(0, cursorPosition) + codePoint + textBoxInput.substring(cursorPosition);
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
            Minecraft.getInstance().keyboardHandler.setClipboard(getTextBoxInput());
            return true;
        } else if (Screen.isPaste(keyCode)) {
            this.setTextBoxInput((textBoxInput.substring(0, cursorPosition)
                    + Minecraft.getInstance().keyboardHandler.getClipboard()
                    + textBoxInput.substring(cursorPosition)));
            return true;
        } else if (Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(getTextBoxInput());
            setTextBoxInput("");

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
                setTextBoxInput("");
                return true;
            }

            textBoxInput =
                    textBoxInput.substring(0, Math.max(0, cursorPosition - 1)) + textBoxInput.substring(cursorPosition);
            setCursorPosition(cursorPosition - 1);
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

    private String getRenderCursorChar(boolean forceUnfocusedCursor) {
        if (isDragging) return ""; // Don't render the cursor if we're dragging

        String cursorChar;
        if (System.currentTimeMillis() - lastCursorSwitch > 350) {
            renderCursor = !renderCursor;
            lastCursorSwitch = System.currentTimeMillis();
        }

        cursorChar = (isFocused() || forceUnfocusedCursor) && renderCursor ? String.valueOf(this.getCursorChar()) : "";
        return cursorChar;
    }

    protected void removeFocus() {
        textboxScreen.setFocusedTextInput(null);
    }

    public void setTextBoxInput(String textBoxInput) {
        this.textBoxInput = textBoxInput;
        setCursorPosition(textBoxInput.length());

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

    private char getCursorChar() {
        return this.DEFAULT_CURSOR_CHAR;
    }

    public String getTextBoxInput() {
        return textBoxInput;
    }

    public void setRenderColor(CustomColor renderColor) {
        this.renderColor = renderColor;
    }

    public CustomColor getRenderColor() {
        return renderColor;
    }

    public String getHighlighted() {
        int start = Math.min(this.cursorPosition, this.highlightPosition);
        int end = Math.max(this.cursorPosition, this.highlightPosition);
        return this.textBoxInput.substring(start, end);
    }

    public boolean hasHighlighted() {
        return this.cursorPosition != this.highlightPosition;
    }

    public void setHighlightPosition(int position) {
        int length = this.textBoxInput.length();
        this.highlightPosition = Mth.clamp(position, 0, length);
    }

    public void replaceHighlighted(String text) {
        int start = Math.min(this.cursorPosition, this.highlightPosition);
        int end = Math.max(this.cursorPosition, this.highlightPosition);
        int length = this.textBoxInput.length() - (start - end);
        int insertLength = text.length();
        if (length < insertLength) {
            text = text.substring(0, length);
            insertLength = length;
        }

        this.textBoxInput = new StringBuilder(this.textBoxInput).replace(start, end, text).toString();
        this.setCursorPosition(start + insertLength);
        this.setHighlightPosition(this.cursorPosition);
        this.onUpdateConsumer.accept(this.textBoxInput);
    }
}
