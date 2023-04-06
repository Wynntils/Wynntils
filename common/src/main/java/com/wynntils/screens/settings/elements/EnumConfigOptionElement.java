/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.text.CodedString;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
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

        this.enumConstants = EnumUtils.getEnumConstants((Class<?>) this.configHolder.getType());
        this.maxOptionWidth = this.enumConstants.stream()
                        .mapToInt(enumValue ->
                                FontRenderer.getInstance().getFont().width(EnumUtils.toNiceString(enumValue)))
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
                        CodedString.fromString(configHolder.getValueString()),
                        0,
                        maxOptionWidth,
                        renderY + FontRenderer.getInstance().getFont().lineHeight / 2f,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        TextShadow.OUTLINE);
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

        McUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());

        return true;
    }
}
