/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.colorpicker;

import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.colorpicker.widgets.AlphaSlider;
import com.wynntils.screens.colorpicker.widgets.HueSlider;
import com.wynntils.screens.colorpicker.widgets.PresetColorButton;
import com.wynntils.screens.colorpicker.widgets.SaturationBrightnessWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ColorPickerScreen extends WynntilsScreen {
    private static final int ROW_LIMIT = 10;
    private static final List<CustomColor> PRESET_COLORS = List.of(
            CommonColors.BLACK,
            CommonColors.RED,
            CommonColors.GREEN,
            CommonColors.BLUE,
            CommonColors.YELLOW,
            CommonColors.BROWN,
            CommonColors.PURPLE,
            CommonColors.CYAN,
            CommonColors.AQUA,
            CommonColors.DARK_AQUA,
            CommonColors.LIGHT_GRAY,
            CommonColors.GRAY,
            CommonColors.DARK_GRAY,
            CommonColors.TITLE_GRAY,
            CommonColors.PINK,
            CommonColors.LIGHT_GREEN,
            CommonColors.LIGHT_BLUE,
            CommonColors.MAGENTA,
            CommonColors.ORANGE,
            CommonColors.WHITE);

    private final Screen previousScreen;
    private final TextInputBoxWidget inputWidget;

    private AlphaSlider alphaSlider;
    private HueSlider hueSlider;
    private SaturationBrightnessWidget saturationBrightnessWidget;
    private TextInputBoxWidget colorInput;

    private CustomColor color;
    private float hue;
    private float saturation;
    private float brightness;

    private int offsetX;
    private int offsetY;

    private ColorPickerScreen(Screen previousScreen, TextInputBoxWidget inputWidget) {
        super(Component.literal("Color Picker Screen"));

        this.previousScreen = previousScreen;
        this.inputWidget = inputWidget;

        color = CustomColor.fromHexString(inputWidget.getTextBoxInput());
    }

    public static Screen create(Screen previousScreen, TextInputBoxWidget inputWidget) {
        return new ColorPickerScreen(previousScreen, inputWidget);
    }

    @Override
    protected void doInit() {
        offsetX = (this.width - Texture.COLOR_PICKER_BACKGROUND.width()) / 2;
        offsetY = (this.height - Texture.COLOR_PICKER_BACKGROUND.height()) / 2;

        saturationBrightnessWidget = new SaturationBrightnessWidget(offsetX + 109, offsetY + 15, 322, 82, this, color);
        this.addRenderableWidget(saturationBrightnessWidget);

        float[] hsbValues = color.asHSB();
        hue = hsbValues[0];
        saturation = hsbValues[1];
        brightness = hsbValues[2];

        hueSlider = new HueSlider(offsetX + 11, offsetY + 105, 420, 20, hue, this);
        this.addRenderableWidget(hueSlider);

        alphaSlider = new AlphaSlider(offsetX + 11, offsetY + 133, 420, 20, color.a() / 255.0, this);
        this.addRenderableWidget(alphaSlider);

        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.colorPicker.cancel"), (button) -> onClose())
                        .pos(offsetX + 40, offsetY + Texture.COLOR_PICKER_BACKGROUND.height() + 5)
                        .size(150, 20)
                        .build());

        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.colorPicker.save"), (button) -> {
                            inputWidget.setTextBoxInput(color.toHexString());
                            onClose();
                        })
                        .pos(
                                offsetX + Texture.COLOR_PICKER_BACKGROUND.width() - 190,
                                offsetY + Texture.COLOR_PICKER_BACKGROUND.height() + 5)
                        .size(150, 20)
                        .build());

        colorInput = new TextInputBoxWidget(offsetX + 11, offsetY + 84, 60, 18, null, this);
        colorInput.setTextBoxInput(color.toHexString());
        this.addRenderableWidget(colorInput);

        this.addRenderableWidget(
                new Button.Builder(Component.literal("✔").withStyle(ChatFormatting.GREEN), (button) -> applyColor())
                        .pos(offsetX + 74, offsetY + 83)
                        .size(20, 20)
                        .build());

        int x = 107;
        int y = 161;

        for (int i = 0; i < PRESET_COLORS.size(); i++) {
            this.addRenderableWidget(
                    new PresetColorButton(offsetX + x, offsetY + y, 12, 12, PRESET_COLORS.get(i), this));

            x += 24;

            if ((i + 1) % ROW_LIMIT == 0) {
                x = 107;
                y += 20;
            }
        }
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.COLOR_PICKER_BACKGROUND, offsetX, offsetY);

        renderSelectedColor(guiGraphics, offsetX + 11, offsetY + 15);

        this.renderables.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;

        color = CustomColor.fromHSV(hue, saturation, brightness, color.a() / 255f);

        colorInput.setTextBoxInput(color.toHexString());
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;

        color = CustomColor.fromHSV(hue, saturation, brightness, color.a() / 255f);

        colorInput.setTextBoxInput(color.toHexString());
    }

    public void setHue(float hue) {
        this.hue = hue;

        float[] hsbColor = color.asHSB();

        color = CustomColor.fromHSV(hue, hsbColor[1], hsbColor[2], color.a() / 255f);

        saturationBrightnessWidget.setColor(CustomColor.fromHSV(hue, 1.0f, 1.0f, color.a() / 255f));

        colorInput.setTextBoxInput(color.toHexString());
    }

    public void setAlpha(int alpha) {
        color = color.withAlpha(alpha);

        colorInput.setTextBoxInput(color.toHexString());
    }

    public void setColor(CustomColor color) {
        this.color = color;

        colorInput.setTextBoxInput(color.toHexString());

        float[] hsbColor = color.asHSB();

        hue = hsbColor[0];
        saturation = hsbColor[1];
        brightness = hsbColor[2];

        hueSlider.setValue(hue);
        saturationBrightnessWidget.setColor(CustomColor.fromHSV(hue, 1.0f, 1.0f, color.a() / 255f));
        saturationBrightnessWidget.updateCursor(saturation, brightness);
        alphaSlider.setValue(color.a() / 255.0);
    }

    public CustomColor getColor() {
        return color;
    }

    private void applyColor() {
        CustomColor newColor = CustomColor.fromHexString(colorInput.getTextBoxInput());

        if (newColor == CustomColor.NONE) return;

        setColor(newColor);
    }

    private void renderSelectedColor(GuiGraphics guiGraphics, int x, int y) {
        RenderUtils.drawRect(guiGraphics.pose(), color, x, y, 0, 82, 66);
    }
}
