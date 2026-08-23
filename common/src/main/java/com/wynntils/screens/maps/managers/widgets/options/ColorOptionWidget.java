/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.options;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
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
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ColorOptionWidget extends AbstractOptionWidget<CustomColor> {
    private static final int SWATCH_BORDER = 3;
    private static final int BUTTON_SIZE = 20;
    private static final int EDIT_BUTTON_WIDTH = 70;
    private static final int ELEMENT_GAP = 5;

    private final TexturedTextInputBoxWidget valueTextBox;
    private final CategoryManagementScreen parent;

    public ColorOptionWidget(
            Component label,
            Component description,
            OptionCategory category,
            Function<MapAttributes, Optional<CustomColor>> valueGetter,
            CategoryManagementScreen parent) {
        super(label, description, 20, category, valueGetter);
        this.parent = parent;
        this.valueTextBox = new TexturedTextInputBoxWidget(
                0, 0, 60, BUTTON_SIZE, this::onTextInputUpdate, parent, TexturedTextInputBoxWidget.Mode.HEXSTRING);
        setValue(defaultValue);
        generateTooltip();
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

        int elementY = getY() + (this.height - BUTTON_SIZE) / 2;

        int editButtonX = getX() + getWidth() - EDIT_BUTTON_WIDTH;
        int textBoxX = editButtonX - ELEMENT_GAP - valueTextBox.getWidth();
        int swatchX = textBoxX - ELEMENT_GAP - BUTTON_SIZE;

        if (isMouseOverEditButton(mouseX, mouseY)) {
            handleCursor(guiGraphics);
        }

        // Swatch
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, swatchX, elementY, BUTTON_SIZE, BUTTON_SIZE);

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
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND, editButtonX, elementY, EDIT_BUTTON_WIDTH, BUTTON_SIZE);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.colorOptionWidget.editColorText")),
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

            setValue(value);
            McUtils.setScreen(ColorPickerScreen.create(McUtils.screen(), valueTextBox));
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private void onTextInputUpdate(String text) {
        CustomColor newValue = CustomColor.fromHexString(text);

        if (newValue != null) {
            super.setValue(newValue);
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
                editButtonY + BUTTON_SIZE - 1);
    }

    @Override
    public void setValue(CustomColor newValue) {
        super.setValue(newValue);
        valueTextBox.setTextBoxInput(newValue.toHexString());
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
