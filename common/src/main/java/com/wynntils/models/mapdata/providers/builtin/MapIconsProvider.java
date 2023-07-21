/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.builtin;

import com.wynntils.models.mapdata.providers.AbstractMapDataProvider;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;

public class MapIconsProvider extends AbstractMapDataProvider {
    private static final List<MapFeatureIcon> PROVIDED_ICONS =
            List.of(new BuiltInIcon("wynntils:icon:waypoint", Texture.WAYPOINT));

    @Override
    public Stream<MapFeatureIcon> getIcons() {
        return PROVIDED_ICONS.stream();
    }

    private static final class BuiltInIcon implements MapFeatureIcon {
        private final String id;
        private final Texture texture;

        private BuiltInIcon(String id, Texture texture) {
            this.id = id;
            this.texture = texture;
        }

        @Override
        public String getIconId() {
            return id;
        }

        @Override
        public ResourceLocation getResourceLocation() {
            return texture.resource();
        }

        @Override
        public int width() {
            return texture.width();
        }

        @Override
        public int height() {
            return texture.height();
        }
    }
}
