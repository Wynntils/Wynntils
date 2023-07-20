package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import net.minecraft.resources.ResourceLocation;

public class JsonIcon implements MapFeatureIcon {
    private final String id;
    private final byte[] texture;
    private final int width;
    private final int height;

    public JsonIcon(String id, byte[] texture, int width, int height) {
        this.id = id;
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    @Override
    public String getIconId() {
        return id;
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return null;
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
