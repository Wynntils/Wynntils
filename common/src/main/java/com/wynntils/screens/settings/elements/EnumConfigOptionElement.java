/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.settings.WynntilsSettingsScreen;
import com.wynntils.screens.settings.widgets.FeatureSettingWidget;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class EnumConfigOptionElement extends ConfigOptionElement {
    private static final CustomColor BORDER_COLOR = new CustomColor(86, 75, 61, 255);
    private static final CustomColor FOREGROUND_COLOR = new CustomColor(228, 199, 156, 255);

    private final float enumSwitchWidth;
    private final List<? extends Enum<?>> enumConstants;

    public EnumConfigOptionElement(
            ConfigHolder configHolder,
            FeatureSettingWidget featureSettingWidget,
            WynntilsSettingsScreen settingsScreen) {
        super(configHolder, featureSettingWidget, settingsScreen);

        this.enumConstants = getEnumConstants();
        this.enumSwitchWidth = this.enumConstants.stream()
                        .mapToInt(enumValue ->
                                FontRenderer.getInstance().getFont().width(enumValue.toString()))
                        .max()
                        .orElse(0)
                + 8;
    }

    @Override
    protected void renderConfigAppropriateButton(
            PoseStack poseStack, float width, float height, int mouseX, int mouseY, float partialTicks) {
        float enumSwitchHeight = getConfigOptionElementSize();

        float renderX = width - enumSwitchWidth * 1.5f;
        float renderY = (height - getConfigOptionElementSize()) / 2f;

        RenderUtils.drawRoundedRectWithBorder(
                poseStack,
                BORDER_COLOR,
                FOREGROUND_COLOR,
                renderX,
                renderY,
                0,
                enumSwitchWidth,
                enumSwitchHeight,
                2,
                2,
                4);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        configHolder.getValue().toString(),
                        renderX,
                        renderX + enumSwitchWidth,
                        renderY + FontRenderer.getInstance().getFont().lineHeight / 2f,
                        0,
                        CommonColors.WHITE,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.OUTLINE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        int addedToIndex;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            addedToIndex = 1;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            addedToIndex = -1;
        } else {
            return;
        }

        McUtils.soundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

        Enum<?> configHolderValue = (Enum<?>) configHolder.getValue();
        assert enumConstants.contains(configHolderValue);

        int nextIndex = (enumConstants.indexOf(configHolderValue) + addedToIndex) % enumConstants.size();
        nextIndex = nextIndex < 0 ? enumConstants.size() + nextIndex : nextIndex;

        Enum<?> nextValue = enumConstants.get(nextIndex);

        configHolder.setValue(nextValue);
    }

    private List<? extends Enum<?>> getEnumConstants() {
        Class<?> clazz = configHolder.getClassOfConfigField();
        assert clazz.isEnum();

        return Arrays.stream(((Class<? extends Enum<?>>) clazz).getEnumConstants())
                .toList();
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}

    @Override
    public float getConfigOptionElementSize() {
        return FontRenderer.getInstance().getFont().lineHeight + 8;
    }
}
