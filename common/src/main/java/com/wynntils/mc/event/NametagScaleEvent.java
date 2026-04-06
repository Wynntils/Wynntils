/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.neoforged.bus.api.Event;

public class NametagScaleEvent extends Event {
    private float scale = 1.0F;

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
