/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.actionbar.event;

import com.wynntils.core.events.WynntilsEvent;
import java.util.regex.Matcher;

public class SpellSegmentUpdateEvent extends WynntilsEvent {
    private final Matcher matcher;

    public SpellSegmentUpdateEvent(Matcher matcher) {
        this.matcher = matcher;
    }

    public Matcher getMatcher() {
        return matcher;
    }
}
