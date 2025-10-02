/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.event;

import com.wynntils.core.events.BaseEvent;
import java.util.regex.Matcher;

public class SpellSegmentUpdateEvent extends BaseEvent {
    private final Matcher matcher;

    public SpellSegmentUpdateEvent(Matcher matcher) {
        this.matcher = matcher;
    }

    public Matcher getMatcher() {
        return matcher;
    }
}
