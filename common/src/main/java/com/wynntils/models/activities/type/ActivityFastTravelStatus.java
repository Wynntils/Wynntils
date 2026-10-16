/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import com.wynntils.core.text.StyledText;
import java.util.regex.Pattern;

public enum ActivityFastTravelStatus {
    AVAILABLE(Pattern.compile(".+§f\uF004\uDB00\uDC02\uF014\uDB00\uDC02\uF001 §a§lShift Right-Click To Fast Travel")),
    UNAVAILABLE(Pattern.compile(".+§f\uF004\uDB00\uDC02\uF014\uDB00\uDC02\uF001 §c§lShift Right-Click To Fast Travel")),
    NOT_ALLOWED(Pattern.compile(".+§cFast Travel is not allowed right now")),
    ON_COOLDOWN(Pattern.compile(".+§cFast Travel Available in( \\d+m)?( \\d+s)?"));

    private final Pattern pattern;

    ActivityFastTravelStatus(Pattern pattern) {
        this.pattern = pattern;
    }

    public static ActivityFastTravelStatus fromLine(StyledText statusLine) {
        for (ActivityFastTravelStatus status : values()) {
            if (statusLine.matches(status.pattern)) return status;
        }

        return null;
    }
}
