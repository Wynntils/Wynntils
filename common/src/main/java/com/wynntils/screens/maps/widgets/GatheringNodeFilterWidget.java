/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.GatheringNodeFilterScreen;
import com.wynntils.services.map.PoiService;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class GatheringNodeFilterWidget extends AbstractWidget {
    private final GatheringNodeFilterScreen filterScreen;
    private final PoiService.GatheringNodeType gatheringNodeType;
    private final Texture icon;
    private final Button toggleButton;
    private final float iconWidth;
    private final float iconHeight;
    private float iconRenderY;

    public GatheringNodeFilterWidget(
            int x,
            int y,
            int width,
            int height,
            GatheringNodeFilterScreen filterScreen,
            PoiService.GatheringNodeType gatheringNodeType) {
        super(
                x,
                y,
                width,
                height,
                Component.literal(gatheringNodeType.sourceMaterial().name()));

        this.filterScreen = filterScreen;
        this.gatheringNodeType = gatheringNodeType;
        this.icon = gatheringNodeType.materialType().getMaterialTexture();

        toggleButton = new Button.Builder(
                        getToggleText(), (button) -> filterScreen.toggleGatheringNodeType(gatheringNodeType))
                .pos(x + width - 60, y)
                .size(55, 20)
                .build();

        float scaleFactor = 0.8f * Math.min(width, height) / Math.max(icon.width(), icon.height());
        iconWidth = icon.width() * scaleFactor;
        iconHeight = icon.height() * scaleFactor;
        updateRenderY(y);
    }

    @Override
    public void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawScalingTexturedRect(guiGraphics, icon, getX() + 2, iconRenderY, iconWidth, iconHeight);

        CustomColor color =
                Services.Poi.isGatheringNodeTypeVisible(gatheringNodeType) ? CommonColors.WHITE : CommonColors.GRAY;

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(gatheringNodeType.sourceMaterial().name()),
                        getX() + 25,
                        getY() + 10,
                        140,
                        color,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(
                                "Level " + gatheringNodeType.sourceMaterial().level()),
                        getX() + 210,
                        getY() + 10,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        toggleButton.setMessage(getToggleText());
        toggleButton.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.isHovered) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        if (toggleButton.mouseClicked(event, isDoubleClick)) return true;

        filterScreen.toggleGatheringNodeType(gatheringNodeType);
        return true;
    }

    public void updateRenderY(int y) {
        setY(y);
        toggleButton.setY(y);
        iconRenderY = (y + height / 2f) - iconHeight / 2f;
    }

    private Component getToggleText() {
        return Services.Poi.isGatheringNodeTypeVisible(gatheringNodeType)
                ? Component.translatable("screens.wynntils.gatheringNodeFilterGui.hide")
                : Component.translatable("screens.wynntils.gatheringNodeFilterGui.show");
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
