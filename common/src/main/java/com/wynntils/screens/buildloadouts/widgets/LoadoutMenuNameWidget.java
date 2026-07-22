package com.wynntils.screens.buildloadouts.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.utils.colors.CommonColors;
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
import org.lwjgl.glfw.GLFW;

public class LoadoutMenuNameWidget extends TextInputBoxWidget {
    private static final int MAX_VISIBLE_CHARARCTERS = 27;
    private static final int EDIT_BUTTON_WIDTH = 23;
    private static final int EDIT_BUTTON_HEIGHT = 20;
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
        super(x, y, width, 20, Component.literal("Loadout Menu Name Widget"), onUpdateConsumer, textboxScreen);
        this.parent = parent;
        this.textPadding = 5;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        if (isEditButtonHovered(mouseX, mouseY)) {
            handleCursor(guiGraphics);
        }

        if (!editing) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            getTruncatedText(StyledText.fromString(parent.getSelectedLoadout().name()), MAX_VISIBLE_CHARARCTERS),
                            this.getX() + textPadding,
                            this.getX() + this.width - EDIT_BUTTON_WIDTH,
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

        renderEditButton(guiGraphics, mouseX, mouseY, partialTick);
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

        if (this.isHovered && !isEditButtonHovered(mouseX, mouseY)) {
            guiGraphics.requestCursor(CursorTypes.IBEAM);
        }

        int textRight = this.getX() + this.width - textPadding - EDIT_BUTTON_WIDTH;

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

    private void renderEditButton(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int btnX = this.getX() + this.width - EDIT_BUTTON_WIDTH;
        int btnY = this.getY() + (this.height - EDIT_BUTTON_HEIGHT) / 2;

        if (!editing) {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_RENAME_ICON,
                    btnX,
                    btnY
            );
        } else {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_RENAME_ICON_WITH_INKWELL,
                    btnX,
                    btnY
            );
        }
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
        return this.width - textPadding * 2 - EDIT_BUTTON_WIDTH;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if (isEditButtonHovered(event.x(), event.y())) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            toggleEditing();
            return true;
        }

        if (!editing) {
            return this.isHovered;
        }

        int textAreaRight = this.getX() + this.width - EDIT_BUTTON_WIDTH;
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

        Loadout selected = parent.getSelectedLoadout();
        if (selected == null) return;

        String oldName = selected.name();
        String newName = getTextBoxInput();

        if (newName.isEmpty() || newName.equals(oldName)) return;

        Services.loadout.setName(oldName, newName);
        parent.setSelectedLoadout(Services.loadout.getLoadout(newName));
        parent.loadoutScrollListWidget.populateLoadouts();
    }

    private boolean isEditButtonHovered(double mouseX, double mouseY) {
        int btnX = this.getX() + this.width - EDIT_BUTTON_WIDTH;
        int btnY = this.getY() + (this.height - EDIT_BUTTON_HEIGHT) / 2;
        return mouseX >= btnX && mouseX <= btnX + EDIT_BUTTON_WIDTH
                && mouseY >= btnY && mouseY <= btnY + EDIT_BUTTON_HEIGHT;
    }

    private StyledText getTruncatedText(StyledText text, int maxVisibleChars) {
        int visibleLength = text.length();
        if (visibleLength <= maxVisibleChars) {
            return text;
        }

        return text.substring(0, maxVisibleChars - 3, StyleType.NONE).append("...");
    }

    public boolean isEditing() {
        return editing;
    }

    public void cancelEditing() {
        if (!editing) return;

        editing = false;
        textboxScreen.setFocusedTextInput(null);
        this.setFocused(false);

        Loadout selected = parent.getSelectedLoadout();
        if (selected != null) {
            this.textBoxInput = selected.name();
            setCursorAndHighlightPositions(this.textBoxInput.length());
        }
    }
}