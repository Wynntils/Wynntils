/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.categorymanagerwidgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.screens.maps.CategoryManagementScreen;
import com.wynntils.screens.maps.type.CategoryTree;
import com.wynntils.screens.maps.type.CategoryTreeNode;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

public class CategoryTreeWidget extends DoubleScrollBarWidget {
    private static final int TREE_TOP_PADDING = 5;

    private final int x;
    private final int y;
    private final CategoryManagementScreen parent;

    private CategoryTree fullTree;
    private CategoryTreeNode filteredRoot;
    private String currentSearch;

    private final List<CategoryTreeEntryWidget> rowWidgets = new ArrayList<>();
    private final Set<String> expandedFullIds = new HashSet<>();
    private String selectedFullId;

    private static final CustomColor LINE_COLOR = CustomColor.fromInt(0x654f3c);

    public CategoryTreeWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, parent);
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    public void setCategories(List<String> categories) {
        fullTree = new CategoryTree(categories);
        filteredRoot = fullTree.getRoot();
        expandedFullIds.clear();
        selectedFullId = null;
        rebuildVisibleRows();
    }

    public void filter(String searchText) {
        if (fullTree == null) return;

        currentSearch = searchText;
        filteredRoot = fullTree.getFilteredTree(searchText);
        scrollOffsetX = 0;
        scrollOffsetY = 0;
        rebuildVisibleRows();
    }

    private void rebuildVisibleRows() {
        rowWidgets.clear();

        if (filteredRoot != null) {
            List<CategoryTreeNode> children = filteredRoot.getChildren();

            for (int i = 0; i < children.size(); i++) {
                addVisibleRows(children.get(i), 0, new boolean[0], i < children.size() - 1);
            }
        }

        updateRowPositions();
        recalculateCanvasSize();
    }

    private void addVisibleRows(
            CategoryTreeNode node,
            int column,
            boolean[] parentTrail,
            boolean hasMoreSiblings) {
        boolean[] trail = Arrays.copyOf(parentTrail, parentTrail.length + 1);
        trail[column] = hasMoreSiblings;

        boolean expanded = isExpanded(node);
        boolean selected = node.getFullId() != null && node.getFullId().equals(selectedFullId);

        CategoryTreeEntryWidget widget = new CategoryTreeEntryWidget(
                0,
                0,
                0,
                node,
                column,
                trail,
                expanded,
                selected,
                () -> toggleExpanded(node),
                () -> selectNode(node));
        rowWidgets.add(widget);

        if (!expanded) return;

        List<CategoryTreeNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            addVisibleRows(children.get(i), column + 1, trail, i < children.size() - 1);
        }
    }

    private boolean isExpanded(CategoryTreeNode node) {
        boolean searching = currentSearch != null && !currentSearch.isBlank();
        return searching || expandedFullIds.contains(node.getFullId());
    }

    private void toggleExpanded(CategoryTreeNode node) {
        if (node.isLeaf() || node.getFullId() == null) return;

        if (!expandedFullIds.add(node.getFullId())) {
            expandedFullIds.remove(node.getFullId());
        }
        rebuildVisibleRows();
    }

    private void selectNode(CategoryTreeNode node) {
        selectedFullId = node.getFullId();
        parent.setSelectedCategory(node.getFullId());
        rebuildVisibleRows();
    }

    private void recalculateCanvasSize() {
        int contentHeight = rowWidgets.size() * CategoryTreeEntryWidget.ROW_HEIGHT+ TREE_TOP_PADDING;

        int maxContentWidth = 0;
        for (CategoryTreeEntryWidget widget : rowWidgets) {
            int rowContentWidth = widget.getColumn() * CategoryTreeEntryWidget.INDENT_WIDTH
                    + widget.computeContentWidth()
                    + CategoryTreeEntryWidget.HORIZONTAL_PADDING;
            maxContentWidth = Math.max(maxContentWidth, rowContentWidth);
        }

        setCanvasSize(maxContentWidth, contentHeight);
    }

    private void updateRowPositions() {
        int baseX = this.x + SCROLL_BAR_WIDTH_PADDING - scrollOffsetX;
        int startY = this.y + SCROLL_BAR_HEIGHT_PADDING + TREE_TOP_PADDING - scrollOffsetY;

        for (int i = 0; i < rowWidgets.size(); i++) {
            CategoryTreeEntryWidget widget = rowWidgets.get(i);
            int rowY = startY + i * CategoryTreeEntryWidget.ROW_HEIGHT;
            int rowX = baseX + widget.getColumn() * CategoryTreeEntryWidget.INDENT_WIDTH;

            widget.setX(rowX);
            widget.setY(rowY);
            widget.setWidth(widget.computeContentWidth());
        }
    }

    @Override
    protected void renderCategoryTree(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (filteredRoot == null || rowWidgets.isEmpty()) return;

        updateRowPositions();

        for (CategoryTreeEntryWidget widget : rowWidgets) {
            // Cull rows outside the scissored area
            if (widget.getY() + CategoryTreeEntryWidget.ROW_HEIGHT < this.y || widget.getY() > this.y + this.height) {
                continue;
            }

            drawConnectorLines(guiGraphics, widget);
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void drawConnectorLines(GuiGraphics guiGraphics, CategoryTreeEntryWidget widget) {
        int column = widget.getColumn();
        if (column == 0) return;

        boolean[] continues = widget.getSiblingContinues();
        int baseX = widget.getX() - column * CategoryTreeEntryWidget.INDENT_WIDTH;
        int rowY = widget.getY();
        int rowHeight = CategoryTreeEntryWidget.ROW_HEIGHT;
        float midY = rowY + rowHeight / 2f;

        for (int c = 0; c <= column; c++) {
            //c-1 is needed. we do not draw the line on our current column but a column to the left.
            float lineX = baseX + (c-1) * CategoryTreeEntryWidget.INDENT_WIDTH + CategoryTreeEntryWidget.ARROW_WIDTH / 2f;

            //if wynntils:service this will draw the service line.
            if (c == column) {
                float endY = continues[column] ? rowY + rowHeight : midY;

                RenderUtils.drawLine(guiGraphics, LINE_COLOR, lineX, rowY, lineX, endY, 1f);
            } else if (c < column) { // this will draw the wynntils line.
                if (!continues[c]) continue;

                RenderUtils.drawLine(guiGraphics, LINE_COLOR, lineX, rowY, lineX, rowY + rowHeight, 1f);
            }
        }

        // Horizontal branch from immediate parent column to node's icon/arrow
        float parentX = baseX + (column - 1) * CategoryTreeEntryWidget.INDENT_WIDTH + CategoryTreeEntryWidget.ARROW_WIDTH / 2f;
        float endX = widget.getNode().isLeaf() ? widget.getIconX() - 2 : widget.getArrowX() - 2;

        RenderUtils.drawLine(guiGraphics, LINE_COLOR, parentX, midY, endX, midY, 1f);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        // Check super first as that handles the scroll bars which should take priority.
        boolean superClicked = super.mouseClicked(event, isDoubleClick);
        if (superClicked) return true;

        for (CategoryTreeEntryWidget widget : rowWidgets) {
            if (widget.isMouseOver(event.x(), event.y())) {
                return widget.mouseClicked(event, isDoubleClick);
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        boolean result = super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        updateRowPositions();
        return result;
    }
}
