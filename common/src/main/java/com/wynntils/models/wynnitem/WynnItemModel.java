/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WynnItemModel extends Model {
    private Map<String, List<ItemObtainInfo>> itemObtainMap = Map.of();
    private List<MaterialConversionInfo> materialConversion = List.of();

    public WynnItemModel() {
        super(List.of());
        loadObtainData();
        loadMaterialConversionData();
    }

    public List<ItemObtainInfo> getObtainInfo(String name) {
        return itemObtainMap.get(name);
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
            materialConversion = WynntilsMod.GSON.fromJson(reader, materialConversionType);
            System.out.println(materialConversion);
        });
    }

    public boolean hasObtainInfo() {
        return !itemObtainMap.isEmpty();
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
