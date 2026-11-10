/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.iconselection;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.IconSelectionScreen;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class IconGridRowWidget extends AbstractWidget {
    public static final int CELL_SIZE = 32;
    private static final int ICON_BORDER = 2;

    private static final CustomColor SELECTED_COLOR = CustomColor.fromInt(0x8033cc33);
    private static final CustomColor HOVERED_COLOR = CustomColor.fromInt(0x40ffffff);

    private final IconSelectionScreen parent;
    private final List<MapIcon> icons;

    public IconGridRowWidget(IconSelectionScreen parent, List<MapIcon> icons) {
        super(0, 0, 0, CELL_SIZE, Component.literal("Icon row"));
        this.parent = parent;
        this.icons = icons;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        String selectedId = parent.getSelectedIconId();

        for (int i = 0; i < icons.size(); i++) {
            MapIcon icon = icons.get(i);
            int cellX = getX() + i * CELL_SIZE;
            int cellY = getY();
            String iconId = icon == null ? MapIcon.NO_ICON_ID : icon.getIconId();
            boolean selected = iconId.equals(selectedId);
            boolean hovered =
                    MathUtils.isInside(mouseX, mouseY, cellX, cellX + CELL_SIZE - 1, cellY, cellY + CELL_SIZE - 1);

            if (selected) {
                RenderUtils.drawRect(guiGraphics, SELECTED_COLOR, cellX, cellY, CELL_SIZE, CELL_SIZE);
                handleCursor(guiGraphics);
            } else if (hovered) {
                RenderUtils.drawRect(guiGraphics, HOVERED_COLOR, cellX, cellY, CELL_SIZE, CELL_SIZE);
                handleCursor(guiGraphics);
            }

            if (icon == null) {
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromComponent(Component.translatable(
                                        "screens.wynntils.map.managers.categoryManager.iconOptionWidget.noneText")),
                                cellX + CELL_SIZE / 2f,
                                cellY + CELL_SIZE / 2f,
                                CommonColors.WHITE,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL);
                continue;
            }

            int iconWidth = icon.getWidth();
            int iconHeight = icon.getHeight();
            float scale = Math.min(1f, (float) (CELL_SIZE - ICON_BORDER * 2) / Math.max(iconWidth, iconHeight));
            int drawWidth = Math.max(1, Math.round(iconWidth * scale));
            int drawHeight = Math.max(1, Math.round(iconHeight * scale));

            RenderUtils.drawScalingTexturedRect(
                    guiGraphics,
                    icon.getIdentifier(),
                    CommonColors.WHITE,
                    cellX + (CELL_SIZE - drawWidth) / 2f,
                    cellY + (CELL_SIZE - drawHeight) / 2f,
                    drawWidth,
                    drawHeight,
                    iconWidth,
                    iconHeight);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;
        if (!isMouseOver(event.x(), event.y())) return false;

        int index = (int) ((event.x() - getX()) / CELL_SIZE);
        if (index < 0 || index >= icons.size()) return false;

        playDownSound(McUtils.mc().getSoundManager());
        MapIcon icon = icons.get(index);
        parent.setSelectedIconId(icon == null ? MapIcon.NO_ICON_ID : icon.getIconId());

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
