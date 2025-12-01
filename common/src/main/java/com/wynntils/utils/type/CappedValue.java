/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import com.google.common.collect.ComparisonChain;

public record CappedValue(int current, int max) implements Comparable<CappedValue> {
    public static final CappedValue EMPTY = new CappedValue(0, 0);

    public static CappedValue fromProgress(float progress, int max) {
        if (max <= 0) {
            return EMPTY;
        }

        int current = Math.round(progress * max);
        return new CappedValue(current, max);
    }

    public CappedValue withCurrent(int newCurrent) {
        return new CappedValue(newCurrent, max);
    }

    /** Return true iff the current value is equal to the max value */
    public boolean isAtCap() {
        return current == max;
    }
    /** Return the difference between the current and the max value */
    public int getRemaining() {
        return max - current;
    }

    /** Return the current value as a percentage of max, in 0..100, rounded
     * to the nearest integer */
    public int getPercentageInt() {
        return Math.round((float) getPercentage());
    }

    /** Return the current value as a percentage of max, in 0..100.0 */
    public double getPercentage() {
        return getProgress() * 100.0;
    }

    /** Return the current value as a proportion of max, in 0..1 */
    public double getProgress() {
        // Treating empty capped values as 100% progress makes reasonable invariants
        // with remaining and atCap hold.
        if (max == 0) return 1.0;

        return (double) current / max;
    }

    @Override
    public String toString() {
        return current + "/" + max;
    }

    @Override
    public int compareTo(CappedValue other) {
        return ComparisonChain.start()
                .compare(current, other.current)
                .compare(max, other.max)
                .result();
    }
}
