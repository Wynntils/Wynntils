/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.statistics.type;

public record StatisticEntry(int total, int count, int min, int max) {
    public static final StatisticEntry EMPTY = new StatisticEntry(0, 0, 0, 0);

    public StatisticEntry getUpdatedEntry(int amount) {
        return new StatisticEntry(total + amount, count + 1, Math.min(min, amount), Math.max(max, amount));
    }

    public int average() {
        if (count == 0) return 0;

        return total / count;
    }
}
