/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.maps.PoiManagementScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class PoiSortButton extends WynntilsButton {
    private final Component title;
    private final PoiManagementScreen managementScreen;
    private final PoiManagementScreen.PoiSortType sortType;

    private boolean ascending = true;
    private StyledText titleToRender;

    public PoiSortButton(
            int x,
            int y,
            int width,
            int height,
            Component title,
            PoiManagementScreen managementScreen,
            PoiManagementScreen.PoiSortType sortType) {
        super(x, y, width, height, title);

        this.title = title;
        this.managementScreen = managementScreen;
        this.sortType = sortType;

        titleToRender = StyledText.fromComponent(title);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRect(
                poseStack, CommonColors.BLACK.withAlpha(isHovered ? 0.5f : 0.3f), getX(), getY(), 0, width, height);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        titleToRender,
                        getX() + 1,
                        getY() + 1,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
    }

    @Override
    public void onPress() {
        managementScreen.toggleSortType(sortType, this);
    }

    public void setSelected(boolean selected) {
        if (!selected) {
            titleToRender = StyledText.fromComponent(title);
            ascending = true;
        } else {
            titleToRender = StyledText.fromComponent(title).append(ascending ? " ʌ" : " v");
            ascending = !ascending;
        }
    }
}
