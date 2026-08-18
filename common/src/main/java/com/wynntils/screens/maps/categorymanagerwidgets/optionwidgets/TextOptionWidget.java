package com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.CategoryManagementScreen;
import com.wynntils.screens.maps.categorymanagerwidgets.TexturedTextInputBoxWidget;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.screens.maps.type.ScrollableWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TextOptionWidget extends AbstractWidget implements ScrollableWidget<String> {
    // Gap between the end of the label text and the start of the text box.
    private static final int LABEL_PADDING = 8;

    private final OptionCategory category;
    private final TexturedTextInputBoxWidget valueTextBox;
    private String value;

    public TextOptionWidget(String label, OptionCategory category, String initialValue, CategoryManagementScreen parent) {
        super(0, 0, 0, 20, Component.literal(label));
        this.category = category;
        this.valueTextBox = new TexturedTextInputBoxWidget(
                getTextboxX(),
                getY(),
                getTextboxWidth(),
                this.height,
                this::onTextInputUpdate,
                parent,
                TexturedTextInputBoxWidget.Mode.STRING);

        setInternalValue(initialValue);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance().renderText(
                guiGraphics,
                StyledText.fromString(getMessage().getString()),
                getX(),
                getY() + this.height / 2f,
                CommonColors.WHITE,
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
        this.value = text;
    }

    private void setInternalValue(String newValue) {
        this.value = newValue == null ? "" : newValue;
        valueTextBox.setTextBoxInput(this.value);
    }

    private int getTextboxX() {
        int labelWidth = FontRenderer.getInstance().getFont().width(getMessage().getString());
        return getX() + labelWidth + LABEL_PADDING;
    }

    private int getTextboxWidth() {
        return Math.max(0, getX() + getWidth() - getTextboxX());
    }

    // ----- ScrollableWidget implementation -----

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String newValue) {
        setInternalValue(newValue);
    }

    @Override
    public OptionCategory getCategory() {
        return category;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isMouseOverTextBox(double mouseX, double mouseY) {
        return valueTextBox.isMouseOver(mouseX, mouseY);
    }

    @Override
    public TextInputBoxWidget getTextInputBoxWidget() {
        return valueTextBox;
    }
}