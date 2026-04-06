/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.impl;

import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.mc.McUtils;
import java.io.IOException;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class MapIconImpl implements MapIcon {
    private final String iconId;
    private final NativeImage nativeImage;
    private final byte[] texture;
    private final int width;
    private final int height;
    private final Identifier resource;

    private boolean registered;

    public MapIconImpl(String iconId, byte[] texture) throws IOException {
        this.iconId = iconId;
        this.nativeImage = NativeImage.read(texture);
        // It's hard to get the bytes back from the native image, so we store them here
        this.texture = texture;
        this.width = nativeImage.getWidth();
        this.height = nativeImage.getHeight();
        this.resource = Identifier.fromNamespaceAndPath("wynntils", "icons/" + iconId.replace(":", "."));
    }

    @Override
    public Identifier getIdentifier() {
        if (!registered) {
            // We cannot do this in the constructor since GL is not initiated at that time
            registered = true;
            McUtils.mc().getTextureManager().register(resource, new DynamicTexture(() -> iconId, nativeImage));
        }

        return resource;
    }

    @Override
    public String getIconId() {
        return iconId;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public byte[] getTextureBytes() {
        return texture;
    }
}
