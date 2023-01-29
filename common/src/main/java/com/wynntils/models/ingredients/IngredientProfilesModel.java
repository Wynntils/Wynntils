/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.gear.profile.IdentificationOrderer;
import com.wynntils.models.ingredients.profile.IngredientProfile;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;

public final class IngredientProfilesModel extends Model {
    private static final Map<ChatFormatting, Integer> TIER_COLOR_CODES = Map.of(
            ChatFormatting.DARK_GRAY, 0,
            ChatFormatting.YELLOW, 1,
            ChatFormatting.LIGHT_PURPLE, 2,
            ChatFormatting.AQUA, 3);

    private IdentificationOrderer identificationOrderer = new IdentificationOrderer(null, null, null);

    private Map<String, IngredientProfile> ingredients = Map.of();
    private Map<String, String> ingredientHeadTextures = Map.of();

    public IngredientProfilesModel() {
        super(List.of());

        loadData();
    }

    public int getTierFromColorCode(String tierColor) {
        return TIER_COLOR_CODES.getOrDefault(ChatFormatting.getByCode(tierColor.charAt(0)), 0);
    }

    public void reloadData() {
        loadData();
    }

    private void loadData() {
        tryLoadIngredientList();
        tryLoadOrderer();
    }

    private void tryLoadIngredientList() {
        // dataAthenaIngredientList is based on
        // https://api.wynncraft.com/v2/ingredient/search/skills/%5Etailoring,armouring,jeweling,cooking,woodworking,weaponsmithing,alchemism,scribing
        // but the data is massaged into another form, and additional "head textures" are added, which are hard-coded
        // in Athena

        Download dl = Managers.Net.download(UrlId.DATA_ATHENA_INGREDIENT_LIST);
        dl.handleJsonObject(json -> {
            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
            ingredientHeadTextures = WynntilsMod.GSON.fromJson(json.getAsJsonObject("headTextures"), hashmapType);

            IngredientProfile[] jsonItems =
                    WynntilsMod.GSON.fromJson(json.getAsJsonArray("ingredients"), IngredientProfile[].class);

            Map<String, IngredientProfile> newIngredients = new HashMap<>();
            for (IngredientProfile ingredientProfile : jsonItems) {
                newIngredients.put(ingredientProfile.getDisplayName(), ingredientProfile);
            }

            ingredients = newIngredients;
        });
    }

    private void tryLoadOrderer() {
        Download dl = Managers.Net.download(UrlId.DATA_ATHENA_ITEM_LIST);
        dl.handleJsonObject(json -> {
            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();

            identificationOrderer =
                    WynntilsMod.GSON.fromJson(json.getAsJsonObject("identificationOrder"), IdentificationOrderer.class);
        });
    }

    public IngredientProfile getIngredient(String name) {
        return ingredients.get(name);
    }

    public String getIngredientHeadTexture(String ingredientName) {
        return ingredientHeadTextures.get(ingredientName);
    }

    public Collection<IngredientProfile> getIngredientsCollection() {
        return ingredients.values();
    }

    public boolean isInverted(String id) {
        return identificationOrderer.isInverted(id);
    }
}
