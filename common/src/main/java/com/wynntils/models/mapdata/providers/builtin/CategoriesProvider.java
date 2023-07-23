/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.builtin;

import com.wynntils.models.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.models.mapdata.attributes.type.MapAttributes;
import com.wynntils.models.mapdata.attributes.type.MapIcon;
import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.services.map.Label;
import com.wynntils.services.map.type.CombatKind;
import com.wynntils.services.map.type.ServiceKind;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CategoriesProvider extends BuiltInProvider {
    private static final List<MapCategory> PROVIDED_CATEGORIES = new ArrayList<>();

    public CategoriesProvider() {
        for (ServiceKind kind : ServiceKind.values()) {
            PROVIDED_CATEGORIES.add(new ServiceCategory(kind));
        }
        for (CombatKind kind : CombatKind.values()) {
            PROVIDED_CATEGORIES.add(new CombatCategory(kind));
        }

        for (Label.LabelLayer layer : Label.LabelLayer.values()) {
            PROVIDED_CATEGORIES.add(new PlaceCategory(layer));
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
            return "All Wynntils Map Features";
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
            return "wynntils:service:" + kind.getServiceId();
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
                    return "wynntils:icon:service:" + kind.getServiceId();
                }

                @Override
                public int getPriority() {
                    return 100;
                }

                @Override
                public CustomColor getLabelColor() {
                    return CommonColors.GREEN;
                }
            };
        }
    }

    private static final class CombatCategory implements MapCategory {
        private final CombatKind kind;

        private CombatCategory(CombatKind kind) {
            this.kind = kind;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:content:" + kind.getServiceId();
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
                    return "wynntils:icon:content:" + kind.getServiceId();
                }

                @Override
                public int getPriority() {
                    return 100;
                }

                @Override
                public CustomColor getLabelColor() {
                    return CommonColors.GREEN;
                }
            };
        }
    }

    private static final class PlaceCategory implements MapCategory {
        private final Label.LabelLayer layer;

        private PlaceCategory(Label.LabelLayer layer) {
            this.layer = layer;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:place:" + layer.getId();
        }

        @Override
        public String getName() {
            return layer.getName();
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public String getIconId() {
                    return MapIcon.NO_ICON_ID;
                }

                @Override
                public CustomColor getLabelColor() {
                    return switch (layer) {
                        case PROVINCE -> CommonColors.AQUA;
                        case CITY -> CommonColors.YELLOW;
                        case TOWN_OR_PLACE -> CommonColors.WHITE;
                    };
                }

                @Override
                public int getPriority() {
                    return 700;
                }
            };
        }
    }
}
