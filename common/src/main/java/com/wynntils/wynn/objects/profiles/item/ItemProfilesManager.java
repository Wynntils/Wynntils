/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

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
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.Utils;
import com.wynntils.wynn.model.GearItemManager;
import com.wynntils.wynn.objects.ItemIdentificationContainer;
import com.wynntils.wynn.objects.SpellType;
import com.wynntils.wynn.objects.profiles.ItemGuessProfile;
import com.wynntils.wynn.objects.profiles.ingredient.IngredientProfile;
import com.wynntils.wynn.utils.WynnUtils;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class ItemProfilesManager extends Manager {
    private static final Gson ITEM_GUESS_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(HashMap.class, new ItemGuessProfile.ItemGuessDeserializer())
            .create();
    public static final NavigableMap<Float, TextColor> COLOR_MAP = new TreeMap<>();
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    static {
        COLOR_MAP.put(0f, TextColor.fromLegacyFormat(ChatFormatting.RED));
        COLOR_MAP.put(70f, TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
        COLOR_MAP.put(90f, TextColor.fromLegacyFormat(ChatFormatting.GREEN));
        COLOR_MAP.put(100f, TextColor.fromLegacyFormat(ChatFormatting.AQUA));
    }

    private IdentificationOrderer identificationOrderer = new IdentificationOrderer(null, null, null);
    private Map<String, ItemProfile> items = Map.of();
    private Map<String, ItemGuessProfile> itemGuesses = Map.of();
    private Map<String, String> translatedReferences = Map.of();
    private Map<String, String> internalIdentifications = Map.of();
    private Map<String, MajorIdentification> majorIdsMap = Map.of();
    private Map<ItemType, String[]> materialTypes = Map.of();
    private Map<String, IngredientProfile> ingredients = Map.of();
    private Map<String, String> ingredientHeadTextures = Map.of();

    public ItemProfilesManager(NetManager netManager, GearItemManager gearItemManager) {
        super(List.of(netManager, gearItemManager));
        loadData();

        // The dependency on GearItemManager is due to the Item model by its
        // GearAnnotator
        // This is slightly hacky, awaiting the full refactoring
        WynntilsMod.registerEventListener(Models.Item);
        Models.Item.init();
    }

    public boolean isInverted(String id) {
        return identificationOrderer.isInverted(id);
    }

    public List<Component> orderComponents(Map<String, Component> holder, boolean groups) {
        return identificationOrderer.orderComponents(holder, groups);
    }

    public List<ItemIdentificationContainer> orderIdentifications(List<ItemIdentificationContainer> ids) {
        return identificationOrderer.orderIdentifications(ids);
    }

    public int getOrder(String id) {
        return identificationOrderer.getOrder(id);
    }

    /**
     * Parse the item ID lore line from a given item, and convert it into an ItemIdentificationContainer
     * Returns null if the given lore line is not a valid ID
     *
     * @param lore the ID lore line component
     * @param item the ItemProfile of the given item
     * @return the parsed ItemIdentificationContainer, or null if invalid lore line
     */
    public ItemIdentificationContainer identificationFromLore(Component lore, ItemProfile item) {
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());
        Matcher identificationMatcher = ITEM_IDENTIFICATION_PATTERN.matcher(unformattedLoreLine);
        if (!identificationMatcher.find()) return null; // not a valid id line

        String idName = identificationMatcher.group("ID");
        boolean isRaw = identificationMatcher.group("Suffix") == null;
        int starCount = identificationMatcher.group("Stars").length();
        int value = Integer.parseInt(identificationMatcher.group("Value"));

        String shortIdName;
        SpellType spell = SpellType.fromName(idName);
        if (spell != null) {
            shortIdName = spell.getShortIdName(isRaw);
        } else {
            shortIdName = IdentificationProfile.getAsShortName(idName, isRaw);
        }

        return identificationFromValue(lore, item, idName, shortIdName, value, starCount);
    }

    /**
     * Creates an ItemIdentificationContainer from the given item, ID names, ID value, and star count
     * Returns null if the given ID is not valid
     *
     * @param lore the ID lore line component - can be null if ID isn't being created from lore
     * @param item the ItemProfile of the given item
     * @param idName the in-game name of the given ID
     * @param shortIdName the internal wynntils name of the given ID
     * @param value the raw value of the given ID
     * @param starCount the number of stars on the given ID
     * @return the parsed ItemIdentificationContainer, or null if the ID is invalid
     */
    public ItemIdentificationContainer identificationFromValue(
            Component lore, ItemProfile item, String idName, String shortIdName, int value, int starCount) {
        IdentificationProfile idProfile = item.getStatuses().get(shortIdName);
        boolean isInverted = idProfile != null ? idProfile.isInverted() : identificationOrderer.isInverted(shortIdName);
        IdentificationModifier type =
                idProfile != null ? idProfile.getType() : IdentificationProfile.getTypeFromName(shortIdName);
        if (type == null) return null; // not a valid id

        MutableComponent percentLine = Component.literal("");

        MutableComponent statInfo = Component.literal((value > 0 ? "+" : "") + value + type.getInGame(shortIdName));
        statInfo.setStyle(Style.EMPTY.withColor(isInverted ^ (value > 0) ? ChatFormatting.GREEN : ChatFormatting.RED));

        percentLine.append(statInfo);

        if (ItemStatInfoFeature.INSTANCE.showStars)
            percentLine.append(Component.literal("***".substring(3 - starCount)).withStyle(ChatFormatting.DARK_GREEN));

        percentLine.append(Component.literal(" " + idName).withStyle(ChatFormatting.GRAY));

        boolean isNew = idProfile == null || idProfile.isInvalidValue(value);

        if (isNew) percentLine.append(Component.literal(" [NEW]").withStyle(ChatFormatting.GOLD));

        MutableComponent rangeLine = percentLine.copy();
        MutableComponent rerollLine = percentLine.copy();

        float percentage = -1;
        if (!isNew && !idProfile.hasConstantValue()) {
            // calculate percent/range/reroll chances, append to lines
            int min = idProfile.getMin();
            int max = idProfile.getMax();

            percentage = MathUtils.inverseLerp(min, max, value) * 100;

            IdentificationProfile.ReidentificationChances chances = idProfile.getChances(value, starCount);

            percentLine.append(getPercentageTextComponent(percentage));

            rangeLine.append(getRangeTextComponent(min, max));

            rerollLine.append(
                    getRerollChancesComponent(idProfile.getPerfectChance(), chances.increase(), chances.decrease()));
        }

        // lore might be null if this ID is not being created from a lore line
        if (lore == null) lore = percentLine;

        // create container
        return new ItemIdentificationContainer(
                item,
                idProfile,
                type,
                shortIdName,
                value,
                starCount,
                percentage,
                lore,
                percentLine,
                rangeLine,
                rerollLine);
    }

    /**
     * Create the colored percentage component for an item ID
     *
     * @param percentage the percent roll of the ID
     * @return the styled percentage text component
     */
    public MutableComponent getPercentageTextComponent(float percentage) {
        Style color = Style.EMPTY
                .withColor(
                        ItemStatInfoFeature.INSTANCE.colorLerp
                                ? getPercentageColor(percentage)
                                : getFlatPercentageColor(percentage))
                .withItalic(false);
        String percentString = new BigDecimal(percentage)
                .setScale(ItemStatInfoFeature.INSTANCE.decimalPlaces, RoundingMode.DOWN)
                .toPlainString();
        return Component.literal(" [" + percentString + "%]").withStyle(color);
    }

    /**
     * Create the colored value range component for an item ID
     *
     * @param min the minimum stat roll
     * @param max the maximum stat roll
     * @return the styled ID range text component
     */
    public MutableComponent getRangeTextComponent(int min, int max) {
        return Component.literal(" [")
                .append(Component.literal(min + ", " + max).withStyle(ChatFormatting.GREEN))
                .append("]")
                .withStyle(ChatFormatting.DARK_GREEN);
    }

    /**
     * Create the colored reroll chance component for an item ID
     *
     * @param perfect the chance of a perfect roll
     * @param increase the chance of an increased roll
     * @param decrease the chance of a decreased roll
     * @return the styled reroll chance text component
     */
    public MutableComponent getRerollChancesComponent(double perfect, double increase, double decrease) {
        return Component.literal(String.format(Utils.getGameLocale(), " \u2605%.2f%%", perfect * 100))
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format(Utils.getGameLocale(), " \u21E7%.1f%%", increase * 100))
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(String.format(Utils.getGameLocale(), " \u21E9%.1f%%", decrease * 100))
                        .withStyle(ChatFormatting.RED));
    }

    private TextColor getPercentageColor(float percentage) {
        Map.Entry<Float, TextColor> lowerEntry = COLOR_MAP.floorEntry(percentage);
        Map.Entry<Float, TextColor> higherEntry = COLOR_MAP.ceilingEntry(percentage);

        // Boundary conditions
        if (lowerEntry == null) {
            return higherEntry.getValue();
        } else if (higherEntry == null) {
            return lowerEntry.getValue();
        }

        if (Objects.equals(lowerEntry.getKey(), higherEntry.getKey())) {
            return lowerEntry.getValue();
        }

        float t = MathUtils.inverseLerp(lowerEntry.getKey(), higherEntry.getKey(), percentage);

        int lowerColor = lowerEntry.getValue().getValue();
        int higherColor = higherEntry.getValue().getValue();

        int r = (int) MathUtils.lerp((lowerColor >> 16) & 0xff, (higherColor >> 16) & 0xff, t);
        int g = (int) MathUtils.lerp((lowerColor >> 8) & 0xff, (higherColor >> 8) & 0xff, t);
        int b = (int) MathUtils.lerp(lowerColor & 0xff, higherColor & 0xff, t);

        return TextColor.fromRgb((r << 16) | (g << 8) | b);
    }

    private TextColor getFlatPercentageColor(float percentage) {
        if (percentage < 30f) {
            return TextColor.fromLegacyFormat(ChatFormatting.RED);
        } else if (percentage < 80f) {
            return TextColor.fromLegacyFormat(ChatFormatting.YELLOW);
        } else if (percentage < 96f) {
            return TextColor.fromLegacyFormat(ChatFormatting.GREEN);
        } else {
            return TextColor.fromLegacyFormat(ChatFormatting.AQUA);
        }
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

            Type materialTypesType = new TypeToken<HashMap<ItemType, String[]>>() {}.getType();
            materialTypes = WynntilsMod.GSON.fromJson(json.getAsJsonObject("materialTypes"), materialTypesType);

            identificationOrderer =
                    WynntilsMod.GSON.fromJson(json.getAsJsonObject("identificationOrder"), IdentificationOrderer.class);

            ItemProfile[] jsonItems = WynntilsMod.GSON.fromJson(json.getAsJsonArray("items"), ItemProfile[].class);
            Map<String, ItemProfile> newItems = new HashMap<>();
            for (ItemProfile itemProfile : jsonItems) {
                itemProfile.getStatuses().forEach((shortId, idProfile) -> idProfile.calculateMinMax(shortId));
                itemProfile.updateMajorIdsFromStrings(majorIdsMap);
                itemProfile.registerIdTypes();

                newItems.put(itemProfile.getDisplayName(), itemProfile);
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

    public ItemProfile getItemsProfile(String name) {
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

    public Collection<ItemProfile> getItemsCollection() {
        return items.values();
    }

    public Collection<IngredientProfile> getIngredientsCollection() {
        return ingredients.values();
    }
}
