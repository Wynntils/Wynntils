/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.neoforged.bus.api.Event;

public class NametagBackgroundOpacityEvent extends Event {
    private float opacity;

    public NametagBackgroundOpacityEvent(float opacity) {
        this.opacity = opacity;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float newOpacity) {
        this.opacity = newOpacity;
    }
}
