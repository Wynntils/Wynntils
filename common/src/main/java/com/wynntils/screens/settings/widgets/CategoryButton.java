/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Category;
import com.wynntils.core.text.StyledText2;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class CategoryButton extends WynntilsButton {
    private final Category category;

    public CategoryButton(int x, int y, int width, int height, Category category) {
        super(x, y, width, height, Component.translatable(category.toString()));
        this.category = category;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText2.of(I18n.get(category.toString())),
                        this.getX(),
                        this.getX() + this.width,
                        this.getY(),
                        0,
                        CommonColors.CYAN,
                        HorizontalAlignment.CENTER,
                        TextShadow.NORMAL);
    }

    @Override
    public void onPress() {}
}
