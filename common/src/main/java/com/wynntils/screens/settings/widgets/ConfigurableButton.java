/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.Configurable;
import com.wynntils.core.features.Translatable;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.CodedString;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.network.chat.Component;

public class ConfigurableButton extends WynntilsButton {
    private final Configurable configurable;

    public ConfigurableButton(int x, int y, int width, int height, Configurable configurable) {
        super(x, y, width, height, Component.literal(((Translatable) configurable).getTranslatedName()));
        this.configurable = configurable;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor color = isHovered ? CommonColors.YELLOW : CommonColors.WHITE;

        if (McUtils.mc().screen instanceof WynntilsBookSettingsScreen bookSettingsScreen) {
            if (bookSettingsScreen.getSelected() == configurable) {
                color = CommonColors.GRAY;
            }
        }

        boolean isOverlay = configurable instanceof Overlay;

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        CodedString.of((isOverlay ? "   " : "") + ((Translatable) configurable).getTranslatedName()),
                        this.getX(),
                        this.getY(),
                        color,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
    }

    @Override
    public void onPress() {
        if (McUtils.mc().screen instanceof WynntilsBookSettingsScreen bookSettingsScreen) {
            bookSettingsScreen.setSelected(configurable);
        }
    }
}
