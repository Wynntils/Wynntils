package com.wynntils.screens.buildloadouts.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class LoadoutMenuNameWidget extends TextInputBoxWidget {
    private static final int EDIT_BUTTON_WIDTH = 20;
    private static final int EDIT_BUTTON_HEIGHT = 20;
    private static final int EDIT_BUTTON_PADDING = 0;
    private static final Component EDIT_ICON = Component.literal("✎");
    private static final Component SAVE_ICON = Component.literal("✓");
    private static final float VERTICAL_OFFSET = 6.5f;

    private final BuildLoadoutsScreen parent;
    private boolean editing = false;

    public LoadoutMenuNameWidget(
            int x,
            int y,
            int width,
            Consumer<String> onUpdateConsumer,
            TextboxScreen textboxScreen,
            BuildLoadoutsScreen parent) {
        super(x, y, width, 20, Component.literal("Loadout Name"), onUpdateConsumer, textboxScreen);
        this.parent = parent;
        this.textPadding = 5;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. Background (your custom nine‑slice)
        renderBackground(guiGraphics);

        // 2. Text area – either static name or active input
        if (!editing) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromString(parent.getSelectedLoadout().name()),
                            this.getX() + textPadding,
                            this.getX() + this.width - EDIT_BUTTON_WIDTH - EDIT_BUTTON_PADDING,
                            this.getY() + VERTICAL_OFFSET,
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            TextShadow.NORMAL);
        } else {
            // Let TextInputBoxWidget calculate scrolling / highlighting,
            // but it will invoke our overridden doRenderWidget (no background).
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }

        // 3. Edit button on top
        renderEditButton(guiGraphics, mouseX, mouseY);
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

        int textRight = this.getX() + this.width - textPadding - EDIT_BUTTON_WIDTH - EDIT_BUTTON_PADDING;

        // First portion
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(firstPortion),
                        this.getX() + textPadding,
                        textRight - lastWidth - highlightedWidth,
                        this.getY() + VERTICAL_OFFSET,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        TextShadow.NORMAL);

        // Highlighted portion
        if (!highlightedPortion.isEmpty()) {
            FontRenderer.getInstance()
                    .renderAlignedHighlightedTextInBox(
                            guiGraphics,
                            StyledText.fromString(highlightedPortion),
                            this.getX() + textPadding + firstWidth,
                            textRight - lastWidth,
                            this.getY() + VERTICAL_OFFSET,
                            this.getY() + VERTICAL_OFFSET,
                            0,
                            CommonColors.BLUE,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP);
        }

        // Last portion
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(lastPortion),
                        this.getX() + textPadding + firstWidth + highlightedWidth,
                        textRight,
                        this.getY() + VERTICAL_OFFSET,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        TextShadow.NORMAL);

        // Cursor
        drawCursor(
                guiGraphics,
                this.getX()
                        + font.width(renderedText.substring(0, Math.min(cursorPosition, renderedText.length())))
                        + textPadding
                        - 2,
                this.getY() + VERTICAL_OFFSET,
                VerticalAlignment.TOP,
                false);
    }

    private void renderEditButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int btnX = this.getX() + this.width - EDIT_BUTTON_WIDTH - EDIT_BUTTON_PADDING;
        int btnY = this.getY() + (this.height - EDIT_BUTTON_HEIGHT) / 2;

        boolean hovered = mouseX >= btnX && mouseX <= btnX + EDIT_BUTTON_WIDTH
                && mouseY >= btnY && mouseY <= btnY + EDIT_BUTTON_HEIGHT;

        // Button bg + border
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_BLUE,
                btnX,
                btnY,
                EDIT_BUTTON_WIDTH,
                EDIT_BUTTON_HEIGHT);

        // Icon
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(editing ? SAVE_ICON.getString() : EDIT_ICON.getString()),
                        btnX,
                        btnX + EDIT_BUTTON_WIDTH,
                        btnY,
                        btnY + EDIT_BUTTON_HEIGHT,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    protected void renderBackground(GuiGraphics guiGraphics) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                getX() - 5,
                getY() - 5,
                this.width + 10,
                this.height + 10);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                getX(),
                getY(),
                this.width,
                this.height);
    }

    @Override
    protected int getMaxTextWidth() {
        // Keep text from rendering underneath the button
        return this.width - textPadding * 2 - EDIT_BUTTON_WIDTH - EDIT_BUTTON_PADDING * 2;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        // Edit button always has priority
        if (isEditButtonHovered(event.x(), event.y())) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            toggleEditing();
            return true;
        }

        // Not editing → ignore clicks on the text area
        if (!editing) {
            return this.isHovered;
        }

        // Editing → normal text field behaviour (restricted to the text area)
        int textAreaRight = this.getX() + this.width - EDIT_BUTTON_WIDTH - EDIT_BUTTON_PADDING;
        if (event.x() >= this.getX()
                && event.x() <= textAreaRight
                && event.y() >= this.getY()
                && event.y() <= this.getY() + this.height) {

            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_2) {
                setTextBoxInput("");
                setCursorAndHighlightPositions(0);
            } else {
                setCursorAndHighlightPositions(getIndexAtPosition(event.x()));
            }
            isDragging = true;
            textboxScreen.setFocusedTextInput(this);
            this.setFocused(true);
            return true;
        } else {
            textboxScreen.setFocusedTextInput(null);
            this.setFocused(false);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!editing) return false;
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (!editing) return false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!editing) return false;

        if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            stopEditing();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!editing) return false;
        return super.charTyped(event);
    }

    @Override
    public boolean isFocused() {
        return editing && textboxScreen.getFocusedTextInput() == this;
    }

    @Override
    protected void removeFocus() {
        if (editing) {
            stopEditing();
        }
        super.removeFocus();
    }

    private void toggleEditing() {
        if (editing) {
            stopEditing();
        } else {
            startEditing();
        }
    }

    private void startEditing() {
        editing = true;
        setTextBoxInput(parent.getSelectedLoadout().name());
        textboxScreen.setFocusedTextInput(this);
        this.setFocused(true);
        setCursorPosition(textBoxInput.length());
    }

    private void stopEditing() {
        editing = false;
        textboxScreen.setFocusedTextInput(null);
        this.setFocused(false);
    }

    private boolean isEditButtonHovered(double mouseX, double mouseY) {
        int btnX = this.getX() + this.width - EDIT_BUTTON_WIDTH - EDIT_BUTTON_PADDING;
        int btnY = this.getY() + (this.height - EDIT_BUTTON_HEIGHT) / 2;
        return mouseX >= btnX && mouseX <= btnX + EDIT_BUTTON_WIDTH
                && mouseY >= btnY && mouseY <= btnY + EDIT_BUTTON_HEIGHT;
    }
}