/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map;

import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class MapProfile {
    NativeImage texture;
    ResourceLocation mapResource;

    boolean registered = false;

    int x1;
    int z1;
    int x2;
    int z2;

    int textureWidth;
    int textureHeight;

    public MapProfile(String name, NativeImage texture, int x1, int z1, int x2, int z2) {
        this.texture = texture;
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
        this.textureWidth = texture.getWidth();
        this.textureHeight = texture.getHeight();

        this.mapResource = new ResourceLocation("wynntils", "/maps/" + name);

        // Remove this if we ever have non 1 to 1 maps
        assert x2 - x1 == textureWidth;
        assert z2 - z1 == textureHeight;
    }

    public ResourceLocation resource() {
        if (!registered) {
            registered = true;
            McUtils.mc().getTextureManager().register(mapResource, new DynamicTexture(texture));
        }

        return mapResource;
    }

    public float getTextureXPosition(double posX) {
        return (float) (posX - x1);
    }

    public float getTextureZPosition(double posZ) {
        return (float) (posZ - z1);
    }

    public int getWorldXPosition(double textureX) {
        return (int) Math.round(textureX + x1);
    }

    public int getWorldZPosition(double textureY) {
        return (int) Math.round(textureY + z1);
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
