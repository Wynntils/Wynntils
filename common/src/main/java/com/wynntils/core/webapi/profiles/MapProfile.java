/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles;

import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class MapProfile {

    DynamicTexture texture;
    ResourceLocation mapResource;

    int x1;
    int z1;
    int x2;
    int z2;

    int textureWidth;
    int textureHeight;

    public MapProfile(DynamicTexture texture, int x1, int z1, int x2, int z2, int textureWidth, int textureHeight) {
        this.texture = texture;
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;

        // Remove this if we ever have non 1 to 1 maps
        assert x2 - x1 == textureWidth;
        assert z2 - z1 == textureHeight;
    }

    public float getTextureXPosition(double posX) {
        return (float) (posX - x1);
    }

    public float getTextureZPosition(double posZ) {
        return (float) (posZ - z2);
    }

    public int getWorldXPosition(double textureX) {
        return (int) Math.round(textureX + x1);
    }

    public int getWorldZPosition(double textureY) {
        return (int) Math.round(textureY + z1);
    }

    public ResourceLocation resource() {
        if (mapResource == null) {
            mapResource = new ResourceLocation("wynntils", "main-map.png");

            McUtils.mc().getTextureManager().register(mapResource, texture);
        }

        return mapResource;
    }

    public int getX1() {
        return x1;
    }

    public int getZ1() {
        return z1;
    }

    public int getX2() {
        return x2;
    }

    public int getZ2() {
        return z2;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public int getTextureWidth() {
        return textureWidth;
    }
}
