/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import com.wynntils.core.text.StyledText;
import java.util.regex.Pattern;

public enum WorldEventFastTravelStatus {
    AVAILABLE(Pattern.compile(".+§f\uE004\uDB00\uDC02\uE014\uDB00\uDC02\uE001 §a§lShift Right-Click To Fast Travel")),
    UNAVAILABLE(Pattern.compile(".+§f\uE004\uDB00\uDC02\uE014\uDB00\uDC02\uE001 §c§lShift Right-Click To Fast Travel")),
    NOT_ALLOWED(Pattern.compile(".+§cFast Travel is not allowed right now")),
    ON_COOLDOWN(Pattern.compile(".+§cFast Travel Available in( \\d+m)?( \\d+s)?"));

    private final Pattern pattern;

    WorldEventFastTravelStatus(Pattern pattern) {
        this.pattern = pattern;
    }

    public static WorldEventFastTravelStatus fromLine(StyledText statusLine) {
        for (WorldEventFastTravelStatus status : values()) {
            if (statusLine.matches(status.pattern)) return status;
        }

        return null;
    }
}
