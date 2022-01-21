/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.Options;
import net.minecraftforge.eventbus.api.Event;

public class OptionsInitEvent extends Event {
    private final Options options;

    public OptionsInitEvent(Options options) {
        this.options = options;
    }

    public Options getOptions() {
        return options;
    }
}
