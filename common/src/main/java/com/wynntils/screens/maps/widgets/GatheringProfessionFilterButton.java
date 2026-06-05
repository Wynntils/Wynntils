/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.models.profession.type.MaterialProfile;
import com.wynntils.screens.maps.GatheringNodeFilterScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class GatheringProfessionFilterButton extends AbstractWidget {
    private final GatheringNodeFilterScreen filterScreen;
    private final MaterialProfile.MaterialType materialType;
    private final Texture icon;
    private final float iconRenderX;
    private final float iconRenderY;
    private final float iconWidth;
    private final float iconHeight;

    private boolean selected;

    public GatheringProfessionFilterButton(
            int x,
            int y,
            int width,
            GatheringNodeFilterScreen filterScreen,
            MaterialProfile.MaterialType materialType,
            boolean selected) {
        super(
                x,
                y,
                width,
                20,
                Component.literal(materialType.getProfessionType().getDisplayName()));

        this.filterScreen = filterScreen;
        this.materialType = materialType;
        this.icon = materialType.getMaterialTexture();
        this.selected = selected;

        float scaleFactor = 0.8f * Math.min(width, height) / Math.max(icon.width(), icon.height());
        iconWidth = icon.width() * scaleFactor;
        iconHeight = icon.height() * scaleFactor;
        iconRenderX = (x + width / 2f) - iconWidth / 2f;
        iconRenderY = (y + height / 2f) - iconHeight / 2f;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), width, height);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                icon.identifier(),
                iconRenderX,
                iconRenderY,
                iconWidth,
                iconHeight,
                icon.width(),
                icon.height());

        if (selected) {
            RenderUtils.drawRect(guiGraphics, CommonColors.LIGHT_BLUE.withAlpha(35), getX(), getY(), width, height);
        }

        if (this.isHovered) {
            guiGraphics.setTooltipForNextFrame(
                    Component.literal(materialType.getProfessionType().getDisplayName()), mouseX, mouseY);
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        selected = !selected;
        filterScreen.toggleMaterialType(materialType, selected, KeyboardUtils.isShiftDown());
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
