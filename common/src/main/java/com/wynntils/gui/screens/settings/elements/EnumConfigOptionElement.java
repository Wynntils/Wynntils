/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class EnumConfigOptionElement extends ConfigOptionElement {
    private static final CustomColor BORDER_COLOR = CommonColors.BLACK;
    private static final CustomColor FOREGROUND_COLOR = new CustomColor(98, 34, 8);
    private static final CustomColor HOVER_FOREGROUND_COLOR = new CustomColor(158, 52, 16);

    private final float maxOptionWidth;
    private final List<? extends Enum<?>> enumConstants;

    public EnumConfigOptionElement(ConfigHolder configHolder) {
        super(configHolder);

        this.enumConstants = getEnumConstants();
        this.maxOptionWidth = this.enumConstants.stream()
                        .mapToInt(enumValue ->
                                FontRenderer.getInstance().getFont().width(enumValue.toString()))
                        .max()
                        .orElse(0)
                + 8;
    }

    @Override
    public void renderConfigAppropriateButton(
            PoseStack poseStack, float width, float height, int mouseX, int mouseY, float partialTicks) {
        final float renderHeight = FontRenderer.getInstance().getFont().lineHeight + 8;

        boolean isHovered = mouseX >= 0 && mouseY >= 0 && mouseX <= maxOptionWidth && mouseY <= renderHeight;

        float renderY = (height - renderHeight) / 2f;

        RenderUtils.drawRoundedRectWithBorder(
                poseStack,
                BORDER_COLOR,
                isHovered ? HOVER_FOREGROUND_COLOR : FOREGROUND_COLOR,
                0,
                renderY,
                0,
                maxOptionWidth,
                renderHeight,
                1,
                3,
                3);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        configHolder.getValue().toString(),
                        0,
                        maxOptionWidth,
                        renderY + FontRenderer.getInstance().getFont().lineHeight / 2f,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.OUTLINE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int addedToIndex;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            addedToIndex = 1;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            addedToIndex = -1;
        } else {
            return false;
        }

        Enum<?> configHolderValue = (Enum<?>) configHolder.getValue();
        assert enumConstants.contains(configHolderValue);

        int nextIndex = (enumConstants.indexOf(configHolderValue) + addedToIndex) % enumConstants.size();
        nextIndex = nextIndex < 0 ? enumConstants.size() + nextIndex : nextIndex;

        Enum<?> nextValue = enumConstants.get(nextIndex);

        configHolder.setValue(nextValue);

        McUtils.playSound(SoundEvents.UI_BUTTON_CLICK.value());

        return true;
    }

    private List<? extends Enum<?>> getEnumConstants() {
        Class<?> clazz = configHolder.getClassOfConfigField();
        assert clazz.isEnum();

        return Arrays.stream(((Class<? extends Enum<?>>) clazz).getEnumConstants())
                .toList();
    }
}
