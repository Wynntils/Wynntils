/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public class UpdateLightTextureEvent extends Event {
    private float gamma;

    public UpdateLightTextureEvent(float gamma) {
        this.gamma = gamma;
    }

    public float getGamma() {
        return gamma;
    }

    public void setGamma(float gamma) {
        this.gamma = gamma;
    }
}
