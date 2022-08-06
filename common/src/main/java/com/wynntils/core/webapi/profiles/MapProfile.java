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

    double rightX;
    double rightZ;
    int imageWidth;
    int imageHeight;

    public MapProfile(DynamicTexture texture, double rightX, double rightZ, int imageWidth, int imageHeight) {
        this.texture = texture;
        this.rightX = rightX;
        this.rightZ = rightZ;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public float getTextureXPosition(double posX) {
        return (float) (posX - rightX + imageWidth);
    }

    public float getTextureZPosition(double posZ) {
        return (float) (posZ - rightZ + imageHeight);
    }

    public int getWorldXPosition(double textureX) {
        return (int) Math.round(textureX + rightX - imageWidth);
    }

    public int getWorldZPosition(double textureY) {
        return (int) Math.round(textureY + rightZ - imageHeight);
    }

    public ResourceLocation resource() {
        if (mapResource == null) {
            mapResource = new ResourceLocation("wynntils", "main-map.png");

            McUtils.mc().getTextureManager().register(mapResource, texture);
        }

        return mapResource;
    }

    public double getRightX() {
        return rightX;
    }

    public double getRightZ() {
        return rightZ;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }
}
