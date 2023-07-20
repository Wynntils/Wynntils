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
    private final byte[] texture;
    private final int width;
    private final int height;
    private boolean registered;
    private ResourceLocation resource;

    public JsonIcon(String id, byte[] texture, int width, int height) {
        this.id = id;
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.resource = new ResourceLocation("wynntils", "icons/" + id.replaceAll(":", "."));
    }

    @Override
    public String getIconId() {
        return id;
    }

    @Override
    public ResourceLocation getResourceLocation() {
        if (!registered) {
            // Needed
            registered = true;
            try {
                NativeImage nativeImage = NativeImage.read(texture);
                McUtils.mc().getTextureManager().register(resource, new DynamicTexture(nativeImage));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
