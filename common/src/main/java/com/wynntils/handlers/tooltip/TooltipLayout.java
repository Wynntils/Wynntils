/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.FontManager;
import com.wynntils.handlers.tooltip.type.TooltipAlignment;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipGrid;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipLinePart;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

final class TooltipLayout {
    // Minimum gap between parts of a line with different alignments
    private static final int DEFAULT_PART_SPACING = 8;

    public static List<Component> alignTooltip(List<? extends TooltipElement> elements) {
        return alignTooltip(elements, 0, DEFAULT_PART_SPACING);
    }

    public static List<Component> alignTooltip(List<? extends TooltipElement> elements, int minimumWidth) {
        return alignTooltip(elements, minimumWidth, DEFAULT_PART_SPACING);
    }

    public static List<Component> alignTooltip(
            List<? extends TooltipElement> elements, int minimumWidth, int partSpacing) {
        List<MeasuredElement> measured =
                elements.stream().map(element -> measure(element, partSpacing)).toList();

        int tooltipWidth = minimumWidth;
        for (MeasuredElement element : measured) {
            tooltipWidth = Math.max(tooltipWidth, element.requiredWidth());
        }

        List<Component> lines = new ArrayList<>();
        for (MeasuredElement element : measured) {
            element.render(tooltipWidth, lines);
        }

        return lines;
    }

    private static MeasuredElement measure(TooltipElement element, int partSpacing) {
        return switch (element) {
            case TooltipLine line -> MeasuredLine.of(line, partSpacing);
            case TooltipGrid grid -> MeasuredGrid.of(grid);
            case TooltipWrappedText text -> new MeasuredWrappedText(text);
        };
    }

    private static int widthOf(Component component) {
        return McUtils.mc().font.width(component);
    }

    private sealed interface MeasuredElement permits MeasuredLine, MeasuredGrid, MeasuredWrappedText {
        int requiredWidth();

        void render(int tooltipWidth, List<Component> output);
    }

    private record MeasuredGroup(List<Component> components, int width) {
        private static MeasuredGroup of(List<TooltipLinePart> parts, TooltipAlignment alignment) {
            List<Component> components = parts.stream()
                    .filter(part -> part.alignment() == alignment)
                    .map(TooltipLinePart::component)
                    .toList();
            int width = components.stream().mapToInt(TooltipLayout::widthOf).sum();
            return new MeasuredGroup(components, width);
        }

        private boolean isEmpty() {
            return components.isEmpty();
        }

        private void appendTo(LineBuilder builder) {
            components.forEach(component -> builder.append(component, widthOf(component)));
        }
    }

    private record MeasuredLine(MeasuredGroup left, List<MeasuredGroup> centers, MeasuredGroup right, int partSpacing)
            implements MeasuredElement {
        @Override
        public int requiredWidth() {
            if (centers.isEmpty()) {
                int spacing = !left.isEmpty() && !right.isEmpty() ? partSpacing : 0;
                return left.width() + spacing + right.width();
            }

            int leftSide = left.isEmpty() ? 0 : left.width() + partSpacing;
            int rightSide = right.isEmpty() ? 0 : right.width() + partSpacing;
            int widestCenter =
                    centers.stream().mapToInt(MeasuredGroup::width).max().orElse(0);

            return widestCenter + 2 * Math.max(leftSide, rightSide);
        }

        @Override
        public void render(int tooltipWidth, List<Component> output) {
            LineBuilder builder = new LineBuilder();

            left.appendTo(builder);
            int contentEnd = builder.cursor();

            for (MeasuredGroup center : centers) {
                int minimumX = left.isEmpty() ? 0 : left.width() + partSpacing;
                builder.moveTo(Math.max(minimumX, TooltipAlignment.CENTER.offset(tooltipWidth, center.width())));
                center.appendTo(builder);
                contentEnd = Math.max(contentEnd, builder.cursor());
            }

            if (!right.isEmpty()) {
                int minimumX = left.isEmpty() && centers.isEmpty() ? 0 : contentEnd + partSpacing;
                builder.moveTo(Math.max(minimumX, TooltipAlignment.RIGHT.offset(tooltipWidth, right.width())));
                right.appendTo(builder);
            }

            output.add(builder.build());
        }

        private static MeasuredLine of(TooltipLine line, int partSpacing) {
            List<MeasuredGroup> centers = line.parts().stream()
                    .filter(part -> part.alignment() == TooltipAlignment.CENTER)
                    .map(part -> new MeasuredGroup(List.of(part.component()), widthOf(part.component())))
                    .toList();

            return new MeasuredLine(
                    MeasuredGroup.of(line.parts(), TooltipAlignment.LEFT),
                    centers,
                    MeasuredGroup.of(line.parts(), TooltipAlignment.RIGHT),
                    partSpacing);
        }
    }

    private record MeasuredGrid(
            TooltipGrid grid,
            List<List<TooltipLinePart>> rows,
            List<int[]> cellWidths,
            List<int[]> rowColumnWidths,
            int[] rowWidths,
            int paddingWidth,
            int spacingWidth,
            int gridWidth)
            implements MeasuredElement {
        @Override
        public int requiredWidth() {
            if (rows.isEmpty()) return 0;

            return Math.max(gridWidth, grid.tableWidth());
        }

        @Override
        public void render(int tooltipWidth, List<Component> output) {
            if (rows.isEmpty()) return;

            TooltipAlignment alignment = grid.alignment();
            int tableWidth = requiredWidth();
            int gridX = alignment.offset(tooltipWidth, tableWidth) + alignment.offset(tableWidth, gridWidth);

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                List<TooltipLinePart> row = rows.get(rowIndex);
                int[] widths = cellWidths.get(rowIndex);
                int[] columnWidths = rowColumnWidths.get(rowIndex);

                LineBuilder builder = new LineBuilder();
                builder.moveTo(gridX + alignment.offset(gridWidth, rowWidths[rowIndex]));
                builder.append(grid.padding(), paddingWidth);

                int columnX = builder.cursor();
                for (int column = 0; column < row.size(); column++) {
                    TooltipLinePart cell = row.get(column);

                    builder.moveTo(columnX + cell.alignment().offset(columnWidths[column], widths[column]));
                    builder.append(cell.component(), widths[column]);

                    columnX += columnWidths[column];
                    if (column < row.size() - 1) {
                        builder.moveTo(columnX);
                        builder.append(grid.spacing(), spacingWidth);
                        columnX += spacingWidth;
                    }
                }

                output.add(builder.build());
            }
        }

        private static MeasuredGrid of(TooltipGrid grid) {
            List<List<TooltipLinePart>> rows = grid.rows();
            boolean fixedColumns = grid.cellWidth() > 0;

            int[] sharedColumnWidths = new int[fixedColumns ? grid.columns() : grid.effectiveColumns()];
            Arrays.fill(sharedColumnWidths, grid.cellWidth());

            List<int[]> cellWidths = new ArrayList<>(rows.size());
            for (List<TooltipLinePart> row : rows) {
                int[] widths = new int[row.size()];
                for (int column = 0; column < row.size(); column++) {
                    widths[column] = widthOf(row.get(column).component());
                    sharedColumnWidths[column] = Math.max(sharedColumnWidths[column], widths[column]);
                }

                cellWidths.add(widths);
            }

            int paddingWidth = widthOf(grid.padding());
            int spacingWidth = widthOf(grid.spacing());

            List<int[]> rowColumnWidths = new ArrayList<>(rows.size());
            int[] rowWidths = new int[rows.size()];
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                int[] columnWidths = fixedColumns ? sharedColumnWidths : cellWidths.get(rowIndex);
                rowColumnWidths.add(columnWidths);

                int columns =
                        fixedColumns ? columnWidths.length : rows.get(rowIndex).size();
                int rowWidth = paddingWidth + spacingWidth * (columns - 1);
                for (int column = 0; column < columns; column++) {
                    rowWidth += columnWidths[column];
                }

                rowWidths[rowIndex] = rowWidth;
            }

            int gridWidth = Arrays.stream(rowWidths).max().orElse(0);

            return new MeasuredGrid(
                    grid, rows, cellWidths, rowColumnWidths, rowWidths, paddingWidth, spacingWidth, gridWidth);
        }
    }

    private record MeasuredWrappedText(TooltipWrappedText text) implements MeasuredElement {
        @Override
        public int requiredWidth() {
            return text.maxWidth() == Integer.MAX_VALUE ? 0 : Math.min(widthOf(text.component()), text.maxWidth());
        }

        @Override
        public void render(int tooltipWidth, List<Component> output) {
            if (tooltipWidth <= 0) {
                output.add(text.component());
                return;
            }

            int wrapWidth = Math.min(tooltipWidth, text.maxWidth());
            for (Component wrapped : ComponentUtils.splitComponent(text.component(), wrapWidth)) {
                int wrappedWidth = widthOf(wrapped);

                LineBuilder builder = new LineBuilder();
                builder.moveTo(text.alignment().offset(tooltipWidth, wrappedWidth));
                builder.append(wrapped, wrappedWidth);

                output.add(builder.build());
            }
        }
    }

    private static final class LineBuilder {
        private final MutableComponent line = Component.empty();
        private int cursor = 0;

        private int cursor() {
            return cursor;
        }

        private void moveTo(int x) {
            String offset = FontManager.calculateOffset(cursor, x);
            if (!offset.isEmpty()) {
                line.append(Component.literal(offset).withStyle(CommonStyles.SPACE));
            }

            cursor = x;
        }

        private void append(Component component, int width) {
            line.append(component);
            cursor += width;
        }

        private Component build() {
            return line;
        }
    }
}
