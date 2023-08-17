/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContainerBounds {
    private final int startRow;
    private final int startCol;
    private final int endRow;
    private final int endCol;
    private final List<Integer> slots = new ArrayList<>();

    /**
     * @param totalRows Total number of rows in the container
     * @param startRow 0-indexed row of the first row containing items
     * @param startCol 0-indexed column of the first column containing items
     * @param endRow 0-indexed row of the last row containing items
     * @param endCol 0-indexed column of the last column containing items
     */
    public ContainerBounds(int startRow, int startCol, int endRow, int endCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;

        // All container slots are numbered from 0 to (totalRows * 9) - 1
        for (int slot = 0; slot < (endRow + 1) * 9; slot++) {
            if (isInBounds(slot)) {
                slots.add(slot);
            }
        }
    }

    private boolean isInBounds(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        return row >= startRow && row <= endRow && col >= startCol && col <= endCol;
    }

    /**
     * @return A list of all non-UI slots in the container
     */
    public List<Integer> getSlots() {
        return Collections.unmodifiableList(slots);
    }
}
