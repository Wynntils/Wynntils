/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.RenderEvent;
import java.awt.*;

public abstract class Overlay
        extends Feature { // extends ScreenRenderer implements SettingsHolder {

    public transient String displayName;
    public transient Point staticSize;
    public transient boolean visible;
    public transient OverlayGrowFrom growthX;
    public transient OverlayGrowFrom growthY;
    public transient RenderEvent.ElementType[] hookElements;

    public Overlay(
            String displayName,
            int sizeX,
            int sizeY,
            boolean visible,
            OverlayGrowFrom growthX,
            OverlayGrowFrom growthY,
            RenderEvent.ElementType... hookElements) {
        super(displayName);
        this.displayName = displayName;
        this.staticSize = new Point(sizeX, sizeY);
        this.visible = visible;
        this.hookElements = hookElements;
        this.growthX = growthX;
        this.growthY = growthY;
    }

    public void render(RenderEvent.Pre e) {}

    public void render(RenderEvent.Post e) {}

    public void tick() {}

    public enum OverlayGrowFrom {
        LEFT,
        CENTER,
        RIGHT;
    }
}
