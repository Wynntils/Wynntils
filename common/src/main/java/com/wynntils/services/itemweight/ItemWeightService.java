/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemweight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.services.itemweight.type.ItemWeighting;
import com.wynntils.services.itemweight.type.WynnpoolWeighting;
import com.wynntils.utils.colors.CustomColor;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class ItemWeightService extends Service {
    private static final Gson WEIGHT_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(
                    WynnpoolWeighting.class, new WynnpoolWeighting.WynnpoolWeightingDeserializer())
            .create();

    private static final ResourceLocation PILL_FONT = ResourceLocation.withDefaultNamespace("banner/pill");
    private static final CustomColor NORI_COLOR = CustomColor.fromInt(0x1cb5fc);
    private static final CustomColor WYNNPOOL_COLOR = CustomColor.fromInt(0xfc9700);
    private static final Style NORI_STYLE = Style.EMPTY.withFont(PILL_FONT).withColor(NORI_COLOR.asInt());
    private static final Style WYNNPOOL_STYLE = Style.EMPTY.withFont(PILL_FONT).withColor(WYNNPOOL_COLOR.asInt());

    public static final Component NORI_HEADER = Component.literal(
                    "\uE060\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE062")
            .withStyle(NORI_STYLE);
    public static final Component WYNNPOOL_HEADER = Component.literal(
                    "\uE060\uDAFF\uDFFF\uE046\uDAFF\uDFFF\uE048\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE03F\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE062")
            .withStyle(WYNNPOOL_STYLE);

    private final Map<String, List<ItemWeighting>> noriWeightings = new HashMap<>();
    private final Map<String, List<ItemWeighting>> wynnpoolWeightings = new HashMap<>();

    public ItemWeightService() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_NORI_ITEM_WEIGHTS).handleReader(this::handleNoriData);
        registry.registerDownload(UrlId.DATA_WYNNPOOL_ITEM_WEIGHTS).handleReader(this::handleWynnPoolData);
    }

    public List<ItemWeighting> getItemWeighting(String itemName, ItemWeightSource source) {
        if (!source.isSingleSource()) {
            return List.of();
        } else {
            return source == ItemWeightSource.NORI
                    ? noriWeightings.getOrDefault(itemName, List.of())
                    : wynnpoolWeightings.getOrDefault(itemName, List.of());
        }
    }

    public float calculateWeighting(ItemWeighting weighting, IdentifiableItemProperty<?, ?> itemInfo) {
        if (weighting == null || itemInfo == null) return 0f;

        List<StatActualValue> identifications = itemInfo.getIdentifications();
        List<StatPossibleValues> possibleValues = itemInfo.getPossibleValues();

        if (identifications == null || possibleValues == null) return 0f;

        double weightedSum = 0;
        double sumWeights = 0;

        for (Map.Entry<String, Double> entry : weighting.identifications().entrySet()) {
            String statApiName = entry.getKey();
            double statWeight = entry.getValue();

            StatActualValue currentValue = identifications.stream()
                    .filter(id -> id.statType().getApiName().equalsIgnoreCase(statApiName))
                    .findFirst()
                    .orElse(null);
            StatPossibleValues statPossibleValues = possibleValues.stream()
                    .filter(id -> id.statType().getApiName().equalsIgnoreCase(statApiName))
                    .findFirst()
                    .orElse(null);

            if (currentValue == null || statPossibleValues == null) continue;

            double percent = StatCalculator.getPercentage(currentValue, statPossibleValues);
            // If the weight is negative, then invert the percentage
            if (statWeight < 0) {
                percent = 100 - percent;
            }

            weightedSum += percent * Math.abs(statWeight);
            sumWeights += Math.abs(statWeight);
        }

        if (sumWeights == 0) return 0f;

        return (float) (weightedSum / sumWeights);
    }

    private void handleNoriData(Reader reader) {
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        JsonObject weightsObj = json.getAsJsonObject("weights");

        for (Map.Entry<String, JsonElement> itemEntry : weightsObj.entrySet()) {
            String itemName = itemEntry.getKey();
            JsonObject weights = itemEntry.getValue().getAsJsonObject();

            for (Map.Entry<String, JsonElement> weightEntry : weights.entrySet()) {
                String weightName = weightEntry.getKey();
                JsonObject identificationsObj = weightEntry.getValue().getAsJsonObject();

                Map<String, Double> identifications = new HashMap<>();
                for (Map.Entry<String, JsonElement> ident : identificationsObj.entrySet()) {
                    identifications.put(ident.getKey(), ident.getValue().getAsDouble() / 100.0);
                }

                ItemWeighting w = new ItemWeighting(weightName, identifications);
                noriWeightings.computeIfAbsent(itemName, k -> new ArrayList<>()).add(w);
            }
        }
    }

    private void handleWynnPoolData(Reader reader) {
        JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();

        for (JsonElement element : jsonArray) {
            WynnpoolWeighting wynnpoolWeighting = WEIGHT_GSON.fromJson(element, WynnpoolWeighting.class);
            String itemName = element.getAsJsonObject().get("item_name").getAsString();

            wynnpoolWeightings.computeIfAbsent(itemName, k -> new ArrayList<>()).add(wynnpoolWeighting.itemWeighting());
        }
    }
}
