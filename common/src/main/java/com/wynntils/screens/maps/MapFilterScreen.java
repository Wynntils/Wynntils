/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.AbstractSideListScreen;
import com.wynntils.screens.maps.widgets.MapFilterWidget;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MapFilterScreen extends AbstractSideListScreen {
    private static final String ROOT_CATEGORY = "wynntils";

    private final List<String> categoriesList = new ArrayList<>();
    private final MainMapScreen mapScreen;

    private String categoryPath;
    private String currentCategory;
    private Set<String> mapFeatures;

    private MapFilterScreen(MainMapScreen mapScreen) {
        super(Component.literal("Map Filter Screen"));

        this.mapScreen = mapScreen;

        currentCategory = ROOT_CATEGORY;
        categoryPath = ROOT_CATEGORY;
        mapFeatures = Stream.concat(
                        Services.MapData.getFeaturesForCategory("wynntils:content:"),
                        Stream.concat(
                                Services.MapData.getFeaturesForCategory("wynntils:service:"),
                                Stream.concat(
                                        Services.MapData.getFeaturesForCategory("wynntils:place:"),
                                        Services.MapData.getFeaturesForCategory("wynntils:fast-travel:"))))
                .map(MapFeature::getCategoryId)
                .collect(Collectors.toSet());

        updateCategoryList();
    }

    public static Screen create(MainMapScreen mapScreen) {
        return new MapFilterScreen(mapScreen);
    }

    @Override
    protected void doInit() {
        super.doInit();

        populateCategories();
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(mapScreen);
    }

    public void selectCategory(String category) {
        currentCategory = category;

        if (categoryPath.isEmpty()) {
            categoryPath += category;
        } else {
            categoryPath += ":" + category;
        }

        updateCategoryList();
        populateCategories();
    }

    public void selectPreviousCategory() {
        String[] pathList = categoryPath.split(":");

        if (pathList.length == 1) {
            currentCategory = "";
        } else {
            currentCategory = pathList[pathList.length - 2];
        }

        String[] newPathList = Arrays.copyOfRange(pathList, 0, pathList.length - 1);
        categoryPath = String.join(":", newPathList);

        updateCategoryList();
        populateCategories();
    }

    @Override
    protected StyledText getEmptyListText() {
        return StyledText.fromComponent(Component.translatable("screens.wynntils.mapFilter.noFeatures"));
    }

    private void populateCategories() {
        sideListWidgets = new ArrayList<>();

        int renderY = 0;

        if (!currentCategory.isEmpty() && !currentCategory.equals(ROOT_CATEGORY)) {
            MapFilterWidget rootCategoryWidget = new MapFilterWidget(
                    renderY, (int) (dividedWidth * 32), (int) (dividedHeight * 4), currentCategory, true);
            sideListWidgets.add(rootCategoryWidget);

            renderY += (int) (dividedHeight * 4);
        }

        for (String category : categoriesList) {
            MapFilterWidget categoryWidget =
                    new MapFilterWidget(renderY, (int) (dividedWidth * 32), (int) (dividedHeight * 4), category, false);
            sideListWidgets.add(categoryWidget);

            renderY += (int) (dividedHeight * 4);
        }

        scroll(scrollOffset);
    }

    private void updateCategoryList() {
        categoriesList.clear();

        categoriesList.addAll(mapFeatures.stream()
                .filter(category -> category.startsWith(categoryPath + ":") || categoryPath.isEmpty())
                .map(category -> {
                    if (categoryPath.isEmpty()) {
                        return category.split(":")[0];
                    } else {
                        return category.split(":")[categoryPath.split(":").length];
                    }
                })
                .filter(category -> !category.isEmpty())
                .collect(Collectors.toSet()));
    }
}
