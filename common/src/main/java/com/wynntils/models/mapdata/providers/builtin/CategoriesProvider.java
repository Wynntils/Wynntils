/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.builtin;

import com.wynntils.models.map.type.ServiceKind;
import com.wynntils.models.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.models.mapdata.attributes.type.MapAttributes;
import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CategoriesProvider extends BuiltInProvider {
    private static final List<MapCategory> PROVIDED_CATEGORIES = new ArrayList<>();

    public CategoriesProvider() {
        for (ServiceKind kind : ServiceKind.values()) {
            PROVIDED_CATEGORIES.add(new ServiceCategory(kind));
        }
        PROVIDED_CATEGORIES.add(new WynntilsCategory());
    }

    @Override
    public String getProviderId() {
        return "categories";
    }

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
            return "Basic Wynntils Map Feature";
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public String getIconId() {
                    return "wynntils:icon:symbols:waypoint";
                }

                @Override
                public int getPriority() {
                    return 500;
                }

                @Override
                public CustomColor getLabelColor() {
                    return CommonColors.WHITE;
                }

                @Override
                public TextShadow getLabelShadow() {
                    return TextShadow.OUTLINE;
                }

                @Override
                public CustomColor getIconColor() {
                    return CommonColors.WHITE;
                }
            };
        }
    }

    private static final class ServiceCategory implements MapCategory {
        private final ServiceKind kind;

        private ServiceCategory(ServiceKind kind) {
            this.kind = kind;
        }

        @Override
        public String getCategoryId() {
            return kind.getCategoryId();
        }

        @Override
        public String getName() {
            return kind.getName();
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public String getLabel() {
                    return kind.getName();
                }

                @Override
                public String getIconId() {
                    return kind.getCategoryId().replace("wynntils:", "wynntils:icon:");
                }

                @Override
                public int getPriority() {
                    return 500;
                }
            };
        }
    }
}
