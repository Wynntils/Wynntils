/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.Texture;
import com.wynntils.mc.objects.CustomColor;
import java.util.Objects;

public class CustomPoi extends StaticIconPoi {
    private String name;
    private CustomColor color;
    private Texture icon;
    private float minZoom;

    public CustomPoi(PoiLocation location, String name, CustomColor color, Texture icon, float minZoom) {
        super(location);

        this.name = name;
        this.color = color;
        this.icon = icon;
        this.minZoom = minZoom;
    }

    @Override
    public Texture getIcon() {
        return icon;
    }

    @Override
    public String getName() {
        return name;
    }

    public CustomColor getColor() {
        return color;
    }

    public float getMinZoom() {
        return minZoom;
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.LOW;
    }

    @Override
    public void renderAt(
            PoseStack poseStack, float renderX, float renderZ, boolean hovered, float scale, float mapZoom) {
        // TODO: Fading
        if (mapZoom < minZoom) return;

        float[] color = this.color.asFloatArray();
        RenderSystem.setShaderColor(color[0], color[1], color[2], 1f);

        super.renderAt(poseStack, renderX, renderZ, hovered, scale, mapZoom);

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        CustomPoi customPoi = (CustomPoi) other;
        return location.equals(customPoi.location)
                && Float.compare(customPoi.minZoom, minZoom) == 0
                && name.equals(customPoi.name)
                && color.equals(customPoi.color)
                && icon == customPoi.icon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, name, color, icon, minZoom);
    }
}
