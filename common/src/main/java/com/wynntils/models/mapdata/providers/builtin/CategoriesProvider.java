/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.builtin;

import com.wynntils.models.mapdata.providers.AbstractMapDataProvider;
import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.attributes.MapFeatureDecoration;
import com.wynntils.models.mapdata.type.attributes.MapFeatureVisibility;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.List;
import java.util.stream.Stream;

public class CategoriesProvider extends AbstractMapDataProvider {
    private static final List<MapFeatureCategory> PROVIDED_CATEGORIES = List.of(new WynntilsCategory());

    @Override
    public Stream<MapFeatureCategory> getCategories() {
        return PROVIDED_CATEGORIES.stream();
    }

    private static final class WynntilsCategory implements MapFeatureCategory {
        @Override
        public String getCategoryId() {
            return "wynntils";
        }

        @Override
        public String getDisplayName() {
            return "Wynntils Map Feature";
        }

        @Override
        public MapFeatureAttributes getAttributes() {
            return new MapFeatureAttributes() {
                @Override
                public String getLabel() {
                    return null;
                }

                @Override
                public String getIconId() {
                    return "wynntils:icon:waypoint";
                }

                @Override
                public int getPriority() {
                    return 900;
                }

                @Override
                public MapFeatureVisibility getLabelVisibility() {
                    return null;
                }

                @Override
                public CustomColor getLabelColor() {
                    return CommonColors.LIGHT_BLUE;
                }

                @Override
                public TextShadow getLabelShadow() {
                    return TextShadow.OUTLINE;
                }

                @Override
                public MapFeatureVisibility getIconVisibility() {
                    return null;
                }

                @Override
                public CustomColor getIconColor() {
                    return CommonColors.BLUE;
                }

                @Override
                public MapFeatureDecoration getIconDecoration() {
                    return null;
                }
            };
        }
    }
}
