/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class IngredientModel extends Model {
    private static final Map<CustomColor, Integer> TIER_COLOR_CODES = Map.of(
            CommonColors.BLACK,
            0,
            CustomColor.fromInt(0xebeb47),
            1,
            CustomColor.fromInt(0xeb47eb),
            2,
            CustomColor.fromInt(0x47ebeb),
            3);
    private final IngredientInfoRegistry ingredientInfoRegistry = new IngredientInfoRegistry();

    public IngredientModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        ingredientInfoRegistry.registerDownloads(registry);
    }

    public int getTierFromColorCode(String tierColor) {
        return TIER_COLOR_CODES.getOrDefault(CustomColor.fromHexString(tierColor), 0);
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
}
