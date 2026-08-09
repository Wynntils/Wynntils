/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.ingredients.type.IngredientPosition;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class IngredientModel extends Model {
    private final IngredientInfoRegistry ingredientInfoRegistry = new IngredientInfoRegistry();

    public IngredientModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        ingredientInfoRegistry.registerDownloads(registry);
    }

    public IngredientInfo getIngredientInfoFromName(String ingredientName) {
        return ingredientInfoRegistry.getFromDisplayName(ingredientName);
    }

    public IngredientInfo getIngredientInfoFromApiName(String ingredientName) {
        return ingredientInfoRegistry.getFromApiName(ingredientName);
    }

    public List<ItemObtainInfo> getObtainInfo(IngredientInfo ingredientInfo) {
        List<ItemObtainInfo> obtainInfo = new ArrayList<>(ingredientInfo.obtainInfo());

        // If the API gave no info, then use the crowd sourced info
        if (obtainInfo.size() == 1 && obtainInfo.getFirst().equals(ItemObtainInfo.UNKNOWN)) {
            obtainInfo.clear();
        }

        obtainInfo.addAll(Models.WynnItem.getObtainInfo(ingredientInfo.name()));
        return obtainInfo;
    }

    public Stream<IngredientInfo> getAllIngredientInfos() {
        return ingredientInfoRegistry.getIngredientInfoStream();
    }

    public static int[][] createPositionModifierGrid(IngredientInfo ingredientInfo) {
        Map<IngredientPosition, Integer> modifiers = ingredientInfo.positionModifiers();
        int left = modifiers.getOrDefault(IngredientPosition.LEFT, 0);
        int right = modifiers.getOrDefault(IngredientPosition.RIGHT, 0);
        int above = modifiers.getOrDefault(IngredientPosition.ABOVE, 0);
        int under = modifiers.getOrDefault(IngredientPosition.UNDER, 0);
        int touching = modifiers.getOrDefault(IngredientPosition.TOUCHING, 0);
        int notTouching = modifiers.getOrDefault(IngredientPosition.NOT_TOUCHING, 0);

        return new int[][] {
            {notTouching, above + notTouching, notTouching},
            {notTouching, above + touching, notTouching},
            {left + touching, 0, right + touching},
            {notTouching, under + touching, notTouching},
            {notTouching, under + notTouching, notTouching}
        };
    }
}
