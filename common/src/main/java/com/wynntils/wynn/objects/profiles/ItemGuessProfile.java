/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
import com.wynntils.wynn.objects.profiles.item.ItemType;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemGuessProfile {
    private final String range;
    private final Map<ItemType, Map<ItemTier, List<String>>> items = new EnumMap<>(ItemType.class);

    public ItemGuessProfile(String range) {
        this.range = range;
    }

    public String getRange() {
        return range;
    }

    public Map<ItemType, Map<ItemTier, List<String>>> getItems() {
        return items;
    }

    public static class ItemGuessDeserializer implements JsonDeserializer<HashMap<?, ?>> {
        @Override
        public HashMap<String, ItemGuessProfile> deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            HashMap<String, ItemGuessProfile> hashMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> itemGuesses : jsonObject.entrySet()) {
                ItemGuessProfile itemGuessProfile = new ItemGuessProfile(itemGuesses.getKey());

                for (Map.Entry<String, JsonElement> weaponType :
                        itemGuesses.getValue().getAsJsonObject().entrySet()) {
                    Map<ItemTier, List<String>> raritiesMap = new EnumMap<>(ItemTier.class);
                    for (Map.Entry<String, JsonElement> rarity :
                            weaponType.getValue().getAsJsonObject().entrySet()) {

                        raritiesMap.put(
                                ItemTier.valueOf(rarity.getKey().toUpperCase(Locale.ROOT)),
                                StringUtils.parseStringToList(rarity.getValue().getAsString()));
                    }

                    itemGuessProfile.items.put(
                            ItemType.valueOf(weaponType.getKey().toUpperCase(Locale.ROOT)), raritiesMap);
                }

                hashMap.put(itemGuesses.getKey(), itemGuessProfile);
            }

            return hashMap;
        }
    }

    @Override
    public String toString() {
        return "ItemGuessProfile{" + "range='" + range + '\'' + ", items=" + items + '}';
    }
}
