/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
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
import java.util.function.Consumer;

import com.wynntils.utils.type.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

// FIXME: Add selection support to this class to be a fully working text box
public class TextInputBoxWidget extends AbstractWidget {
    private static final char DEFAULT_CURSOR_CHAR = '|';
    private final Consumer<String> onUpdateConsumer;
    protected String textBoxInput = "";
    private int cursorPosition = 0;
    private int highlightPosition = 0;
    private long lastCursorSwitch = 0;
    private boolean renderCursor = true;
    private CustomColor renderColor = CommonColors.WHITE;

    protected final TextboxScreen textboxScreen;

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

        Pair<String, Pair<Integer, Integer>> renderedTextDetails = getRenderedText(this.width - 8);
        String renderedText = renderedTextDetails.a();
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        renderedText,
                        2,
                        this.width,
                        2,
                        this.height - 2,
                        0,
                        renderColor,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);

        if (this.hasHighlighted()) {
            renderHighlight(renderedTextDetails.b().a(), 2, renderedTextDetails.b().b(), this.height - 2);
        }

        poseStack.popPose();
    }

    protected Pair<String, Pair<Integer, Integer>> getRenderedText(float maxTextWidth) {
        Font font = FontRenderer.getInstance().getFont();

        String cursorChar = getRenderCursorChar();
        final int cursorWidth = font.width(cursorChar);

        String entireText = textBoxInput.substring(0, cursorPosition) + cursorChar + textBoxInput.substring(cursorPosition);
        if (font.width(entireText) < maxTextWidth) {
            if (this.hasHighlighted()) {
                int highlightStart = font.width(textBoxInput.substring(0, Math.min(cursorPosition, highlightPosition)));
                int highlightWidth = font.width(textBoxInput.substring(Math.min(cursorPosition, highlightPosition), Math.max(cursorPosition, highlightPosition)));
                return new Pair<>(entireText, new Pair<>(highlightStart, highlightWidth));
            } else {
                return new Pair<>(entireText, new Pair<>(0, 0));
            }
        }

        StringBuilder builder = new StringBuilder();
        int highlightStart;
        int highlightWidth;

        // First append to the left of the cursor
        int stringPosition = cursorPosition - 1;
        while (font.width(builder.toString()) < maxTextWidth - cursorWidth && stringPosition >= 0) {
            builder.append(textBoxInput.charAt(stringPosition));

            stringPosition--;
        }

        if (Math.min(cursorPosition, highlightPosition) < stringPosition) {
            highlightStart = 0;
        } else {
            highlightStart = font.width(textBoxInput.substring(stringPosition + 1, Math.min(cursorPosition, highlightPosition)));
        }

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

        return new Pair<>(builder.toString(), new Pair<>(highlightStart, (int) Math.min(font.width(textBoxInput.substring(Math.min(cursorPosition, highlightPosition), Math.max(cursorPosition, highlightPosition))), maxTextWidth - cursorWidth - highlightStart)));
    }

    private void renderHighlight(int startX, int startY, int endX, int endY) {
        if (startX < endX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        if (startY < endY) {
            int i = startY;
            startY = endY;
            endY = i;
        }

        if (endX > this.getX() + this.width) {
            endX = this.getX() + this.width;
        }

        if (startX > this.getX() + this.width) {
            startX = this.getX() + this.width;
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(startX, endY, 0.0).endVertex();
        bufferBuilder.vertex(endX, endY, 0.0).endVertex();
        bufferBuilder.vertex(endX, startY, 0.0).endVertex();
        bufferBuilder.vertex(startX, startY, 0.0).endVertex();
        tesselator.end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        McUtils.playSound(SoundEvents.UI_BUTTON_CLICK.value());

        if (this.isHovered) {
            //setCursorAndHighlightPositions(getIndexAtPosition(mouseX, mouseY));
            textboxScreen.setFocusedTextInput(this);
            return true;
        } else {
            textboxScreen.setFocusedTextInput(null);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
            setCursorAndHighlightPositions(Screen.hasControlDown() ? 0 : cursorPosition - 1);
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            setCursorAndHighlightPositions(Screen.hasControlDown() ? textBoxInput.length() : cursorPosition + 1);
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return true;
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

    private String getRenderCursorChar() {
        String cursorChar;
        if (System.currentTimeMillis() - lastCursorSwitch > 350) {
            renderCursor = !renderCursor;
            lastCursorSwitch = System.currentTimeMillis();
        }

        cursorChar = isFocused() && renderCursor ? String.valueOf(this.getCursorChar()) : "";
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

    public void setCursorPosition(int cursorPosition) {
        this.cursorPosition = MathUtils.clamp(cursorPosition, 0, textBoxInput.length());
    }

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
