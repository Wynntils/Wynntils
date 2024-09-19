/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.models.wynnitem.type.ItemObtainType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;

public class IngredientModel extends Model {
    private static final Map<ChatFormatting, Integer> TIER_COLOR_CODES = Map.of(
            ChatFormatting.DARK_GRAY, 0,
            ChatFormatting.YELLOW, 1,
            ChatFormatting.LIGHT_PURPLE, 2,
            ChatFormatting.AQUA, 3);

    private final IngredientInfoRegistry ingredientInfoRegistry = new IngredientInfoRegistry();

    public IngredientModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        ingredientInfoRegistry.registerDownloads(registry);
    }

    public int getTierFromColorCode(String tierColor) {
        return TIER_COLOR_CODES.getOrDefault(ChatFormatting.getByCode(tierColor.charAt(0)), 0);
    }

    public IngredientInfo getIngredientInfoFromName(String ingredientName) {
        return ingredientInfoRegistry.getFromDisplayName(ingredientName);
    }

    public IngredientInfo getIngredientInfoFromApiName(String ingredientName) {
        return ingredientInfoRegistry.getFromApiName(ingredientName);
    }

    public List<ItemObtainInfo> getObtainInfo(IngredientInfo ingredientInfo) {
        List<ItemObtainInfo> obtainInfo = Models.WynnItem.getObtainInfo(ingredientInfo.name());
        if (obtainInfo == null) {
            return List.of(new ItemObtainInfo(ItemObtainType.UNKNOWN, Optional.empty()));
        }
        return obtainInfo;
    }

    public Stream<IngredientInfo> getAllIngredientInfos() {
        return ingredientInfoRegistry.getIngredientInfoStream();
    }
}
