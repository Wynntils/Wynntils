/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.SideListWidget;
import com.wynntils.screens.maps.WaypointCategoryScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;

public class CategoryWidget extends SideListWidget {
    private final String category;
    private final boolean rootCategory;

    public CategoryWidget(int y, int width, int height, String category, boolean rootCategory) {
        super(y, width, height);

        this.category = category;
        this.rootCategory = rootCategory;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack poseStack = guiGraphics.pose();

        if (rootCategory) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("⏎"),
                            getX() + 10,
                            getY() + getHeight() / 2f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            FontRenderer.getInstance()
                    .renderScrollingText(
                            poseStack,
                            StyledText.fromString(category),
                            getX() + 20,
                            getY() + getHeight() / 2f,
                            getWidth() - 30,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else {
            FontRenderer.getInstance()
                    .renderScrollingText(
                            poseStack,
                            StyledText.fromString(category),
                            getX() + 10,
                            getY() + getHeight() / 2f,
                            getWidth() - 20,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (McUtils.mc().screen instanceof WaypointCategoryScreen categoryScreen) {
            if (rootCategory) {
                categoryScreen.selectPreviousCategory();
            } else {
                categoryScreen.selectCategory(category);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
