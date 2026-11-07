/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.iconselection;

import com.wynntils.screens.maps.managers.IconSelectionScreen;
import com.wynntils.screens.maps.managers.widgets.ScrollBarWidget;
import com.wynntils.services.mapdata.type.MapIcon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

public class IconGridScrollBarWidget extends ScrollBarWidget {
    private static final int GRID_COLUMNS = 10;

    private final IconSelectionScreen parent;
    private final List<IconGridRowWidget> rows = new ArrayList<>();

    public IconGridScrollBarWidget(int x, int y, int width, int height, IconSelectionScreen parent) {
        super(x, y, width, height);
        this.parent = parent;
    }

    public void refreshIcons(boolean scrollToSelection) {
        List<MapIcon> all = new ArrayList<>();
        all.add(null); // "None" cell
        all.addAll(parent.getIconsForSelectedProvider());

        rows.clear();
        for (int i = 0; i < all.size(); i += GRID_COLUMNS) {
            List<MapIcon> chunk = new ArrayList<>(all.subList(i, Math.min(i + GRID_COLUMNS, all.size())));
            rows.add(new IconGridRowWidget(parent, chunk));
        }

        if (scrollToSelection) {
            scrollOffsetY = getScrollOffsetForIcon(all, parent.getSelectedIconId());
        }

        updateWidgetPositions();
    }

    private int getScrollOffsetForIcon(List<MapIcon> icons, String selectedId) {
        if (selectedId == null) return 0;

        int index = -1;
        for (int i = 0; i < icons.size(); i++) {
            MapIcon icon = icons.get(i);
            if ((icon == null ? MapIcon.NO_ICON_ID : icon.getIconId()).equals(selectedId)) {
                index = i;
                break;
            }
        }
        if (index < 0) return 0;

        int rowTop = (index / GRID_COLUMNS) * IconGridRowWidget.CELL_SIZE;
        return Math.max(0, rowTop - (getViewportHeight() - IconGridRowWidget.CELL_SIZE) / 2);
    }

    @Override
    protected List<IconGridRowWidget> getWidgets() {
        return Collections.unmodifiableList(rows);
    }

    @Override
    protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        getWidgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (super.mouseClicked(event, isDoubleClick)) {
            return true;
        }

        if (isInsideViewport(event.x(), event.y())) {
            for (IconGridRowWidget row : getWidgets()) {
                if (!row.visible || !row.isMouseOver(event.x(), event.y())) continue;

                if (row.mouseClicked(event, isDoubleClick)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void onScrollOffsetChanged() {
        updateWidgetPositions();
    }

    public void updateWidgetPositions() {
        int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int viewportBottom = viewportTop + getViewportHeight();
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
        int currentY = viewportTop - scrollOffsetY;

        for (IconGridRowWidget row : getWidgets()) {
            int rowHeight = row.getHeight();

            if (currentY + rowHeight >= viewportTop && currentY <= viewportBottom) {
                row.visible = true;
                row.setX(viewportLeft);
                row.setY(currentY);
                row.setWidth(getViewportWidth());
            } else {
                row.visible = false;
            }

            currentY += rowHeight;
        }
    }
}
