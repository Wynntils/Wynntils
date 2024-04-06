/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

public final class StatListDelimiter extends StatType {
    private static int count = 1;

    public StatListDelimiter() {
        super("DELIMITER_" + count++, "<N/A>", "<N/A>", "<N/A>", StatUnit.RAW);
    }
}
