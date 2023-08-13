/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard.type;

import java.util.regex.Pattern;

public record SegmentMatcher(Pattern headerPattern) {
    public static SegmentMatcher fromPattern(String pattern) {
        return new SegmentMatcher(Pattern.compile(pattern));
    }
}
