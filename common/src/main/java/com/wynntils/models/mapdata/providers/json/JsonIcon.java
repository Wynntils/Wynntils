/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.json;

import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.utils.mc.McUtils;
import java.io.IOException;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class JsonIcon implements MapFeatureIcon {
    private final String id;
    private final NativeImage nativeImage;
    private final int width;
    private final int height;
    private boolean registered;
    private ResourceLocation resource;

    public JsonIcon(String id, byte[] texture) throws IOException {
        this.id = id;
        this.nativeImage = NativeImage.read(texture);
        this.width = nativeImage.getWidth();
        ;
        this.height = nativeImage.getHeight();
        ;
        this.resource = new ResourceLocation("wynntils", "icons/" + id.replaceAll(":", "."));
    }

    @Override
    public String getIconId() {
        return id;
    }

    @Override
    public ResourceLocation getResourceLocation() {
        if (!registered) {
            // We canot do this in the constructor since GL is not initiated by then
            registered = true;
            McUtils.mc().getTextureManager().register(resource, new DynamicTexture(nativeImage));
        }

        return resource;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }
}
