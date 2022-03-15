/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.mc.event.RenderEvent;
import java.awt.*;

public abstract class Overlay
        extends Feature { // extends ScreenRenderer implements SettingsHolder {

    public transient Point staticSize;
    public transient boolean visible;
    public transient OverlayGrowFrom growthX;
    public transient OverlayGrowFrom growthY;
    public transient RenderEvent.ElementType[] hookElements;

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    @Override
    protected boolean onEnable() {
        FeatureRegistry.enableOverlay(this);
        return true;
    }

    @Override
    protected void onDisable() {
        FeatureRegistry.disableOverlay(this);
    }

    public Overlay(
            int sizeX,
            int sizeY,
            boolean visible,
            OverlayGrowFrom growthX,
            OverlayGrowFrom growthY,
            RenderEvent.ElementType... hookElements) {
        this.staticSize = new Point(sizeX, sizeY);
        this.visible = visible;
        this.hookElements = hookElements;
        this.growthX = growthX;
        this.growthY = growthY;
    }

    public abstract boolean render(RenderEvent.Pre e);

    public abstract void render(RenderEvent.Post e);

    public abstract void tick();

    public enum OverlayGrowFrom {
        LEFT,
        CENTER,
        RIGHT,
        NONE;
    }
}
