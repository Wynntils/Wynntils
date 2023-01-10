/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.actionbar.event;

import java.util.regex.Matcher;
import net.minecraftforge.eventbus.api.Event;

public class SpellSegmentUpdateEvent extends Event {
    private final Matcher matcher;

    public SpellSegmentUpdateEvent(Matcher matcher) {
        this.matcher = matcher;
    }

    public Matcher getMatcher() {
        return matcher;
    }
}
