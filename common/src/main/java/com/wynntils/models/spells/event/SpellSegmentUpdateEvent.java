/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.event;

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
