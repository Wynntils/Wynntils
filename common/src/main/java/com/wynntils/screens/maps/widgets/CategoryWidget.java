/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.WaypointCategoryScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class CategoryWidget extends AbstractWidget {
    private final boolean rootCategory;
    private final String category;

    public CategoryWidget(int y, int width, int height, String category, boolean rootCategory) {
        super(0, y, width, height, Component.literal(category));

        this.category = category;
        this.rootCategory = rootCategory;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics,
                CommonColors.BROWN.withAlpha(isHovered ? 150 : 100),
                this.getX(),
                this.getY(),
                width,
                height);

        if (rootCategory) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("⏎"),
                            getX() + 10,
                            getY() + getHeight() / 2f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            FontRenderer.getInstance()
                    .renderScrollingText(
                            guiGraphics,
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
                            guiGraphics,
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
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (McUtils.mc().screen instanceof WaypointCategoryScreen categoryScreen) {
            if (rootCategory) {
                categoryScreen.selectPreviousCategory();
            } else {
                categoryScreen.selectCategory(category);
            }
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
