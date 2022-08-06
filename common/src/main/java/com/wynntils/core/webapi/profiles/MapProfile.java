package com.wynntils.core.webapi.profiles;

import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.resources.ResourceLocation;

public class MapProfile {

    ResourceLocation mapResource;

    double rightX;
    double rightZ;
    int imageWidth;
    int imageHeight;

    public MapProfile(ResourceLocation mapResource, double rightX, double rightZ, int imageWidth, int imageHeight) {
        this.mapResource = mapResource;
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


