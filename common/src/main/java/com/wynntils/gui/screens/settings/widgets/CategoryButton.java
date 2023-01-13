/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.widgets.WynntilsButton;
import com.wynntils.mc.objects.CommonColors;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class CategoryButton extends WynntilsButton {
    private final FeatureCategory featureCategory;

    public CategoryButton(int x, int y, int width, int height, FeatureCategory featureCategory) {
        super(x, y, width, height, Component.translatable(featureCategory.toString()));
        this.featureCategory = featureCategory;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        I18n.get(featureCategory.toString()),
                        this.getX(),
                        this.getX() + this.width,
                        this.getY(),
                        0,
                        CommonColors.CYAN,
                        HorizontalAlignment.Center,
                        TextShadow.NORMAL);
    }

    @Override
    public void onPress() {}
}
