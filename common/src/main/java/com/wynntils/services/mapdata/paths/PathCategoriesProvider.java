/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.impl.MapCategoryImpl;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.utils.colors.CommonColors;
import java.util.List;
import java.util.stream.Stream;

public final class PathCategoriesProvider extends BuiltInProvider {
    public static final String PATH_CATEGORY = "wynntils:path";
    public static final String RECORDING_CATEGORY = "wynntils:path-recording";
    public static final String ARROW_ICON = "wynntils:icon:path:arrows";
    private final List<MapCategory> categories = List.of(
            new MapCategoryImpl(
                    PATH_CATEGORY,
                    "Map paths",
                    new MapAttributesBuilder()
                            .setPriority(900)
                            .asPathAttributes()
                            .build()),
            new MapCategoryImpl(
                    RECORDING_CATEGORY,
                    "Recording paths",
                    new MapAttributesBuilder()
                            .setPriority(900)
                            .setPathColor(CommonColors.RED)
                            .setPathFillColor(CommonColors.RED)
                            .asPathAttributes()
                            .build()));

    @Override
    public String getProviderId() {
        return "path-categories";
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return categories.stream();
    }

    @Override
    public void reloadData() {}
}
