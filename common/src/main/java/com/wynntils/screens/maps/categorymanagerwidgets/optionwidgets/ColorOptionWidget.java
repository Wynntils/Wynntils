package com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.screens.maps.CategoryManagementScreen;
import com.wynntils.screens.maps.categorymanagerwidgets.TexturedTextInputBoxWidget;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.screens.maps.type.ScrollableWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ColorOptionWidget extends AbstractWidget implements ScrollableWidget<CustomColor> {
    private static final int SWATCH_BORDER = 3;
    private static final int BUTTON_SIZE = 20;
    private static final int EDIT_BUTTON_WIDTH = 70;
    private static final int ELEMENT_GAP = 5;

    private final OptionCategory category;
    private CustomColor value;
    private final TexturedTextInputBoxWidget valueTextBox;
    private final CategoryManagementScreen parent;

    public ColorOptionWidget(String label, OptionCategory category, CustomColor initialColor, CategoryManagementScreen parent) {
        super(0, 0, 0, 20, Component.literal(label));
        this.category = category;
        this.value = initialColor;
        this.parent = parent;
        this.valueTextBox = new TexturedTextInputBoxWidget(0, 0, 60, BUTTON_SIZE, this::onTextInputUpdate, parent, TexturedTextInputBoxWidget.Mode.HEXSTRING);
        this.valueTextBox.setTextBoxInput(initialColor.toHexString());
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

        int elementY = getY() + (this.height - BUTTON_SIZE) / 2;

        int editButtonX = getX() + getWidth() - EDIT_BUTTON_WIDTH;
        int textBoxX = editButtonX - ELEMENT_GAP - valueTextBox.getWidth();
        int swatchX = textBoxX - ELEMENT_GAP - BUTTON_SIZE;

        if (isMouseOverEditButton(mouseX, mouseY)) {
            handleCursor(guiGraphics);
        }

        // Swatch
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_TEXT_BOX_BACKGROUND,
                swatchX,
                elementY,
                BUTTON_SIZE,
                BUTTON_SIZE);

        guiGraphics.fill(
                swatchX + SWATCH_BORDER,
                elementY + SWATCH_BORDER,
                swatchX + BUTTON_SIZE - SWATCH_BORDER,
                elementY + BUTTON_SIZE - SWATCH_BORDER,
                value.asInt());

        // Value text box
        valueTextBox.setX(textBoxX);
        valueTextBox.setY(elementY);
        valueTextBox.render(guiGraphics, mouseX, mouseY, partialTick);

        // Edit button
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_WIDGET_BACKGROUND,
                editButtonX,
                elementY,
                EDIT_BUTTON_WIDTH,
                BUTTON_SIZE);

        FontRenderer.getInstance().renderText(
                guiGraphics,
                StyledText.fromString("Edit Color"),
                editButtonX + EDIT_BUTTON_WIDTH / 2f,
                elementY + BUTTON_SIZE / 2f,
                CommonColors.WHITE,
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if (isMouseOverEditButton(event.x(), event.y())) {
            this.playDownSound(McUtils.mc().getSoundManager());

            valueTextBox.setTextBoxInput(value.toHexString());
            McUtils.setScreen(ColorPickerScreen.create(McUtils.screen(), valueTextBox));
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private void onTextInputUpdate(String text) {
        try {
            this.value = CustomColor.fromHexString(text);
        } catch (Exception ignored) {
            // Keep the previous value while the input is mid-edit / invalid.
        }
    }

    private boolean isMouseOverEditButton(double mouseX, double mouseY) {
        int editButtonY = getY() + (this.height - BUTTON_SIZE) / 2;
        int editButtonX = getX() + getWidth() - EDIT_BUTTON_WIDTH;

        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                editButtonX,
                editButtonX + EDIT_BUTTON_WIDTH - 1,
                editButtonY,
                editButtonY + BUTTON_SIZE - 1
        );
    }

    // ----- ScrollableWidget implementation -----

    @Override
    public CustomColor getValue() {
        return value;
    }

    @Override
    public void setValue(CustomColor newValue) {
        this.value = newValue;
        this.valueTextBox.setTextBoxInput(newValue.toHexString());
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