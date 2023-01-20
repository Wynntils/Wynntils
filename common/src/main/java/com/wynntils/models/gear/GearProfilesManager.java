/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.gear.profile.GearProfile;
import com.wynntils.models.gear.profile.IdentificationOrderer;
import com.wynntils.models.gear.profile.ItemGuessProfile;
import com.wynntils.models.gear.profile.MajorIdentification;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.ingredients.profile.IngredientProfile;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class GearProfilesManager extends Manager {
    private static final Gson ITEM_GUESS_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(HashMap.class, new ItemGuessProfile.ItemGuessDeserializer())
            .create();
    private static final Map<ChatFormatting, Integer> LEVEL_COLORS = Map.of(
            ChatFormatting.DARK_GRAY, 0,
            ChatFormatting.YELLOW, 1,
            ChatFormatting.LIGHT_PURPLE, 2,
            ChatFormatting.AQUA, 3);

    private IdentificationOrderer identificationOrderer = new IdentificationOrderer(null, null, null);
    private Map<String, GearProfile> items = Map.of();
    private Map<String, ItemGuessProfile> itemGuesses = Map.of();
    private Map<String, String> translatedReferences = Map.of();
    private Map<String, String> internalIdentifications = Map.of();
    private Map<String, MajorIdentification> majorIdsMap = Map.of();
    private Map<GearType, String[]> materialTypes = Map.of();
    private Map<String, IngredientProfile> ingredients = Map.of();
    private Map<String, String> ingredientHeadTextures = Map.of();

    public GearProfilesManager(NetManager netManager, GearItemManager gearItemManager) {
        super(List.of(netManager, gearItemManager));
        loadData();

        // The dependency on GearItemManager is due to the Item model by its
        // GearAnnotator
        // This is slightly hacky, awaiting the full refactoring
        WynntilsMod.registerEventListener(Models.Item);
        Models.Item.init();
    }

    public int getTierFromColorCode(String tierColor) {
        return LEVEL_COLORS.getOrDefault(ChatFormatting.getByCode(tierColor.charAt(0)), 0);
    }

    public IdentificationOrderer getIdentificationOrderer() {
        return identificationOrderer;
    }

    public boolean isInverted(String id) {
        return identificationOrderer.isInverted(id);
    }

    public List<Component> orderComponents(Map<String, Component> holder, boolean groups) {
        return identificationOrderer.orderComponents(holder, groups);
    }

    public List<GearIdentificationContainer> orderIdentifications(List<GearIdentificationContainer> ids) {
        return identificationOrderer.orderIdentifications(ids);
    }

    public int getOrder(String id) {
        return identificationOrderer.getOrder(id);
    }

    public void reloadData() {
        loadData();
    }

    private void loadData() {
        tryLoadItemList();
        tryLoadItemGuesses();
        tryLoadIngredientList();
    }

    private void tryLoadItemGuesses() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_ITEM_GUESSES);
        dl.handleReader(reader -> {
            Type type = new TypeToken<HashMap<String, ItemGuessProfile>>() {}.getType();
            Map<String, ItemGuessProfile> newItemGuesses = new HashMap<>(ITEM_GUESS_GSON.fromJson(reader, type));
            itemGuesses = newItemGuesses;
        });
    }

    private void tryLoadItemList() {
        // dataAthenaItemList is based on
        // https://api.wynncraft.com/public_api.php?action=itemDB&category=all
        // but the data is massaged into another form, and wynnBuilderID is injected from
        // https://wynnbuilder.github.io/compress.json

        Download dl = Managers.Net.download(UrlId.DATA_ATHENA_ITEM_LIST);
        dl.handleJsonObject(json -> {
            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
            translatedReferences = WynntilsMod.GSON.fromJson(json.getAsJsonObject("translatedReferences"), hashmapType);

            internalIdentifications =
                    WynntilsMod.GSON.fromJson(json.getAsJsonObject("internalIdentifications"), hashmapType);

            Type majorIdsType = new TypeToken<HashMap<String, MajorIdentification>>() {}.getType();
            majorIdsMap = WynntilsMod.GSON.fromJson(json.getAsJsonObject("majorIdentifications"), majorIdsType);

            Type materialTypesType = new TypeToken<HashMap<GearType, String[]>>() {}.getType();
            materialTypes = WynntilsMod.GSON.fromJson(json.getAsJsonObject("materialTypes"), materialTypesType);

            identificationOrderer =
                    WynntilsMod.GSON.fromJson(json.getAsJsonObject("identificationOrder"), IdentificationOrderer.class);

            GearProfile[] jsonItems = WynntilsMod.GSON.fromJson(json.getAsJsonArray("items"), GearProfile[].class);
            Map<String, GearProfile> newItems = new HashMap<>();
            for (GearProfile gearProfile : jsonItems) {
                gearProfile.getStatuses().forEach((shortId, idProfile) -> idProfile.calculateMinMax(shortId));
                gearProfile.updateMajorIdsFromStrings(majorIdsMap);
                gearProfile.registerIdTypes();

                newItems.put(gearProfile.getDisplayName(), gearProfile);
            }

            items = newItems;
        });
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

    public ItemGuessProfile getItemGuess(String levelRange) {
        return itemGuesses.get(levelRange);
    }

    public GearProfile getItemsProfile(String name) {
        return items.get(name);
    }

    public String getInternalIdentification(String internalId) {
        return internalIdentifications.get(internalId);
    }

    public String getTranslatedReference(String untranslatedName) {
        return translatedReferences.getOrDefault(untranslatedName, untranslatedName);
    }

    public IngredientProfile getIngredient(String name) {
        return ingredients.get(name);
    }

    public String getIngredientHeadTexture(String ingredientName) {
        return ingredientHeadTextures.get(ingredientName);
    }

    public Collection<GearProfile> getItemsCollection() {
        return items.values();
    }

    public Collection<IngredientProfile> getIngredientsCollection() {
        return ingredients.values();
    }
}
