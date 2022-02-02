/*
 *  * Copyright © Wynntils - 2022.
 */

package com.wynntils.core.features.overlays;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.RenderEvent;
import net.minecraft.client.Minecraft;

import java.awt.*;


public abstract class Overlay extends Feature { // extends ScreenRenderer implements SettingsHolder {

    public transient String displayName;
    public transient Point staticSize;
    public transient boolean visible;
    public transient OverlayGrowFrom growth;
    public transient RenderEvent.ElementType[] hookElements;

    public Overlay(String displayName, int sizeX, int sizeY, boolean visible, float anchorX, float anchorY, int offsetX, int offsetY, OverlayGrowFrom growth, RenderEvent.ElementType... hookElements) {
        this.displayName = displayName;
        this.staticSize = new Point(sizeX, sizeY);
        this.visible = visible;
        this.hookElements = hookElements;
        this.growth = growth;
    }

    public void render(RenderEvent.Pre e) {}
    public void render(RenderEvent.Post e) {}
    public void tick() {}

    public enum OverlayGrowFrom {

        TOP_LEFT, TOP_CENTRE , TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_CENTRE, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTRE, BOTTOM_RIGHT;

    }

}