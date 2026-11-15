/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * @param cells the cells, the alignment of each cell is the alignment of its content within the cell
 * @param columns the maximum number of cells per row
 * @param cellWidth the width of each column, columns are never made narrower than their widest cell
 * @param tableWidth the minimum width the grid takes up in the tooltip, or 0 for no minimum
 * @param alignment the alignment of the grid as a whole within the tooltip
 * @param padding prepended to each row, before the first cell
 * @param spacing placed between each column
 */
public record TooltipGrid(
        List<TooltipLinePart> cells,
        int columns,
        int cellWidth,
        int tableWidth,
        TooltipAlignment alignment,
        Component padding,
        Component spacing)
        implements TooltipElement {
    public TooltipGrid {
        if (columns <= 0) {
            throw new IllegalArgumentException("Grid must have at least one column, got " + columns);
        }

        cells = List.copyOf(cells);
    }

    public static Builder builder(int columns) {
        return new Builder(columns);
    }

    /**
     * The number of columns actually used, a grid with fewer cells than columns only uses as many columns as cells
     */
    public int effectiveColumns() {
        return Math.min(columns, cells.size());
    }

    public List<List<TooltipLinePart>> rows() {
        List<List<TooltipLinePart>> rows = new ArrayList<>();
        for (int i = 0; i < cells.size(); i += columns) {
            rows.add(cells.subList(i, Math.min(i + columns, cells.size())));
        }

        return rows;
    }

    public static final class Builder {
        private final List<TooltipLinePart> cells = new ArrayList<>();
        private final int columns;
        private int cellWidth = 0;
        private int tableWidth = 0;
        private TooltipAlignment alignment = TooltipAlignment.LEFT;
        private Component padding = Component.empty();
        private Component spacing = Component.empty();

        private Builder(int columns) {
            this.columns = columns;
        }

        public Builder cellWidth(int cellWidth) {
            this.cellWidth = cellWidth;
            return this;
        }

        public Builder tableWidth(int tableWidth) {
            this.tableWidth = tableWidth;
            return this;
        }

        public Builder alignment(TooltipAlignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public Builder padding(Component padding) {
            this.padding = padding;
            return this;
        }

        public Builder spacing(Component spacing) {
            this.spacing = spacing;
            return this;
        }

        public Builder add(TooltipLinePart cell) {
            cells.add(cell);
            return this;
        }

        public Builder add(Component cell, TooltipAlignment cellAlignment) {
            return add(new TooltipLinePart(cell, cellAlignment));
        }

        public Builder addAll(List<TooltipLinePart> cells) {
            this.cells.addAll(cells);
            return this;
        }

        public TooltipGrid build() {
            return new TooltipGrid(cells, columns, cellWidth, tableWidth, alignment, padding, spacing);
        }
    }
}
