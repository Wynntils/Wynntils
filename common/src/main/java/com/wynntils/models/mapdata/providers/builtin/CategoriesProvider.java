/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.builtin;

import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.attributes.MapAttributes;
import com.wynntils.models.mapdata.type.attributes.MapDecoration;
import com.wynntils.models.mapdata.type.attributes.MapVisibility;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.List;
import java.util.stream.Stream;

public class CategoriesProvider extends AbstractMapDataProvider {
    private static final List<MapCategory> PROVIDED_CATEGORIES = List.of(new WynntilsCategory());

    @Override
    public Stream<MapCategory> getCategories() {
        return PROVIDED_CATEGORIES.stream();
    }

    private static final class WynntilsCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils";
        }

        @Override
        public String getName() {
            return "Wynntils Map Feature";
        }

        @Override
        public MapAttributes getAttributes() {
            return new MapAttributes() {
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
                public MapVisibility getLabelVisibility() {
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
                public MapVisibility getIconVisibility() {
                    return null;
                }

                @Override
                public CustomColor getIconColor() {
                    return CommonColors.BLUE;
                }

                @Override
                public MapDecoration getIconDecoration() {
                    return null;
                }
            };
        }
    }
}
