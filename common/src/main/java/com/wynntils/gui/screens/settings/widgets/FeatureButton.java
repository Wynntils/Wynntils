/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.Feature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public class FeatureButton extends AbstractButton {
    private final Feature feature;

    public FeatureButton(int x, int y, int width, int height, Feature feature) {
        super(x, y, width, height, new TextComponent(feature.getTranslatedName()));
        this.feature = feature;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor color = isHovered ? CommonColors.YELLOW : CommonColors.WHITE;

        if (McUtils.mc().screen instanceof WynntilsBookSettingsScreen bookSettingsScreen) {
            if (bookSettingsScreen.getSelected() == feature) {
                color = CommonColors.GRAY;
            }
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        feature.getTranslatedName(),
                        this.x,
                        this.y,
                        color,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NORMAL);
    }

    @Override
    public void onPress() {
        if (McUtils.mc().screen instanceof WynntilsBookSettingsScreen bookSettingsScreen) {
            bookSettingsScreen.setSelected(feature);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
