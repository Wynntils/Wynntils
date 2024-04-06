/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.models.wynnitem.type.ItemObtainType;
import com.wynntils.models.wynnitem.type.MaterialConversionInfo;
import com.wynntils.utils.JsonUtils;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WynnItemModel extends Model {
    private Map<String, List<ItemObtainInfo>> itemObtainMap = Map.of();
    private List<MaterialConversionInfo> allMaterialConversions = List.of();
    private Map<Integer, MaterialConversionInfo> materialConversionLookup = Map.of();

    public WynnItemModel() {
        super(List.of());

        loadObtainData();
        loadMaterialConversionData();
    }

    public List<ItemObtainInfo> getObtainInfo(String name) {
        return itemObtainMap.getOrDefault(name, List.of());
    }

    public Optional<String> getMaterialName(int idCode, int damageCode) {
        MaterialConversionInfo conversionInfo = allMaterialConversions.stream()
                .filter(c -> c.id() == idCode && c.type() == damageCode)
                .findFirst()
                .orElse(null);
        if (conversionInfo == null) return Optional.empty();

        return Optional.of(conversionInfo.name());
    }

    private void loadObtainData() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_ITEM_OBTAIN);
        dl.handleReader(reader -> {
            Type obtainType = new TypeToken<Map<String, List<ItemObtainInfo>>>() {}.getType();
            Gson gson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(ItemObtainInfo.class, new ItemObtainInfoDeserializer())
                    .create();
            itemObtainMap = gson.fromJson(reader, obtainType);
        });
    }

    private void loadMaterialConversionData() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_MATERIAL_CONVERSION);
        dl.handleReader(reader -> {
            Type materialConversionType = new TypeToken<List<MaterialConversionInfo>>() {}.getType();
            allMaterialConversions = WynntilsMod.GSON.fromJson(reader, materialConversionType);

            // Store in fast lookup map
            Map<Integer, MaterialConversionInfo> lookupMap = new HashMap<>();
            allMaterialConversions.forEach(m -> lookupMap.put(m.id(), m));
            materialConversionLookup = lookupMap;
        });
    }

    public boolean hasObtainInfo() {
        return !itemObtainMap.isEmpty();
    }

    public boolean hasMaterialConversionInfo() {
        return !allMaterialConversions.isEmpty();
    }

    private static final class ItemObtainInfoDeserializer implements JsonDeserializer<ItemObtainInfo> {
        @Override
        public ItemObtainInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();
            String sourceTypeStr = json.get("type").getAsString();
            ItemObtainType sourceType = ItemObtainType.fromApiName(sourceTypeStr);
            String name = JsonUtils.getNullableJsonString(json, "name");

            // FIXME: We are ignoring the details field for now...

            return new ItemObtainInfo(sourceType, Optional.ofNullable(name));
        }
    }
}
