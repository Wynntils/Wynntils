/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import com.wynntils.utils.StringUtils;

public record Time(long timestamp) {
    public static Time of(long timestamp) {
        return new Time(timestamp);
    }

    @Override
    public String toString() {
        return StringUtils.getRelativeTimeString(timestamp);
    }
}
