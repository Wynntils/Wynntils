/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

public final class StatListSeparator extends StatType {
    private static int count = 1;
    public StatListSeparator() {
        super("SEPARATOR_" + count++, "<N/A>", "<N/A>", "<N/A>", StatUnit.RAW);
    }
}
