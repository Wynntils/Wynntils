/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.RenderEvent;

public abstract class Overlay extends Feature { // extends ScreenRenderer implements SettingsHolder {

    public transient OverlayPosition position;
    public transient boolean visible;
    // public transient OverlayGrowFrom growthX;
    // public transient OverlayGrowFrom growthY;
    public transient RenderEvent.ElementType[] hookElements; // Not sure if this is needed

    public Overlay(
            float x,
            float y,
            int width,
            int height,
            boolean visible,
            // OverlayGrowFrom growthX,
            // OverlayGrowFrom growthY,
            RenderEvent.ElementType... hookElements) {
        this.position = new OverlayPosition(x, y, width, height);
        this.visible = visible;
        this.hookElements = hookElements;
        // this.growthX = growthX;
        // this.growthY = growthY;
    }

    public abstract void render(RenderEvent.Pre e);

    public abstract void render(RenderEvent.Post e);

    public abstract void tick();

    public enum OverlayGrowFrom {
        LEFT,
        CENTER,
        RIGHT;
    }
}
