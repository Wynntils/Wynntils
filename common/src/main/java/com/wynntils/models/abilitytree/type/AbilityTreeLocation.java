/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.google.common.collect.ComparisonChain;

/**
 * Represents a location on the ability tree.
 *
 * @param page The page of the ability tree. Index starts at 1.
 * @param row The row of the ability tree. Index starts at 0.
 * @param col The column of the ability tree. Index starts at 0.
 */
public record AbilityTreeLocation(int page, int row, int col) implements Comparable<AbilityTreeLocation> {
    private static final int MAX_ROWS = 6;
    private static final int MAX_COLS = 9;

    public static AbilityTreeLocation fromSlot(int slot, int page) {
        int row = slot / MAX_COLS;
        int col = slot % MAX_COLS;

        return new AbilityTreeLocation(page, row, col);
    }

    private int getAbsoluteRow() {
        return (page - 1) * MAX_ROWS + row;
    }

    // A location is a neighbor of another location if it is on the same row or column and is 1 away
    // (diagonal is not allowed)
    public boolean isNeighbor(AbilityTreeLocation other) {
        return (getAbsoluteRow() == other.getAbsoluteRow() && Math.abs(col - other.col) == 1)
                || (col == other.col && Math.abs(getAbsoluteRow() - other.getAbsoluteRow()) == 1);
    }

    public AbilityTreeLocation up() {
        if (row == 0) {
            return null;
        }

        return new AbilityTreeLocation(page, row - 1, col);
    }

    public AbilityTreeLocation down() {
        if (row + 1 == MAX_ROWS) {
            return new AbilityTreeLocation(page + 1, 0, col);
        }

        return new AbilityTreeLocation(page, row + 1, col);
    }

    public AbilityTreeLocation left() {
        if (col == 0) {
            return null;
        }

        return new AbilityTreeLocation(page, row, col - 1);
    }

    public AbilityTreeLocation right() {
        if (col + 1 == MAX_COLS) {
            return null;
        }

        return new AbilityTreeLocation(page, row, col + 1);
    }

    @Override
    public int compareTo(AbilityTreeLocation other) {
        return ComparisonChain.start()
                .compare(getAbsoluteRow(), other.getAbsoluteRow())
                .compare(col, other.col)
                .result();
    }
}
