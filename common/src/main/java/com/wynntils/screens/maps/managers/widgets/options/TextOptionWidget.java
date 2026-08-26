/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.options;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TextOptionWidget extends AbstractOptionWidget<String> {
    // Gap between the end of the label text and the start of the text box.
    private static final int LABEL_PADDING = 8;

    private final TexturedTextInputBoxWidget valueTextBox;

    public TextOptionWidget(
            Component label,
            Component description,
            OptionCategory category,
            Function<MapAttributes, Optional<String>> valueGetter,
            CategoryManagementScreen parent) {
        super(label, description, 18, category, valueGetter);
        this.valueTextBox = new TexturedTextInputBoxWidget(
                getTextboxX(),
                getY(),
                getTextboxWidth(),
                this.height,
                this::onTextInputUpdate,
                parent,
                TexturedTextInputBoxWidget.Mode.STRING);

        setValue(defaultValue == null ? "" : defaultValue);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(getMessage().getString()),
                        getX(),
                        getY() + this.height / 2f,
                        !this.inherited || isChanged() ? CommonColors.WHITE : CommonColors.GRAY,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        valueTextBox.setX(getTextboxX());
        valueTextBox.setY(getY());
        valueTextBox.setWidth(getTextboxWidth());
        valueTextBox.setHeight(this.height);
        valueTextBox.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        return valueTextBox.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return valueTextBox.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return valueTextBox.mouseReleased(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private void onTextInputUpdate(String text) {
        super.setValue(text);
    }

    private int getTextboxX() {
        int labelWidth = FontRenderer.getInstance().getFont().width(getMessage().getString());
        return getX() + labelWidth + LABEL_PADDING;
    }

    private int getTextboxWidth() {
        return Math.max(0, getX() + getWidth() - getTextboxX());
    }

    @Override
    public void setValue(String newValue) {
        super.setValue(newValue);
        valueTextBox.setTextBoxInput(newValue);
    }

    @Override
    public boolean isMouseOverTextBox(double mouseX, double mouseY) {
        return valueTextBox.isMouseOver(mouseX, mouseY);
    }

    @Override
    public TextInputBoxWidget getTextInputBoxWidget(double mouseX, double mouseY) {
        return valueTextBox;
    }
}
