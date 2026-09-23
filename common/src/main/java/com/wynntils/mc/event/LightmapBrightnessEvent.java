/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.neoforged.bus.api.Event;

public class LightmapBrightnessEvent extends Event {
    private float brightnes;

    public LightmapBrightnessEvent(float brightnes) {
        this.brightnes = brightnes;
    }

    public float getBrightnes() {
        return brightnes;
    }

    public void setBrightnes(float brightnes) {
        this.brightnes = brightnes;
    }
}
