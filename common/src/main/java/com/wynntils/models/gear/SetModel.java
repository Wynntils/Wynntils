/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.gear.type.SetBonus;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.properties.SetItemProperty;
import com.wynntils.models.stats.type.StatType;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

public final class SetModel extends Model {
    // Stored as a map for quick lookup <name, SetInfo>
    private final Map<String, SetInfo> setData = new LinkedHashMap<>();

    public SetModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_SETS).handleReader(this::handleSetData);
    }

    public Collection<SetInfo> getSets() {
        return setData.values();
    }

    /**
     * @param setName Name of the set as it appears in game. Eg. "Morph"
     */
    public SetInfo getSetInfo(String setName) {
        return setData.getOrDefault(setName, null);
    }

    public SetInfo getSetInfoForItem(String itemName) {
        return getSetInfo(getSetName(itemName));
    }

    /**
     * @param itemName The name of the item to check
     * @return The set name if the item is part of a set, null otherwise
     */
    public String getSetName(String itemName) {
        for (Map.Entry<String, SetInfo> entry : setData.entrySet()) {
            if (entry.getValue().items().contains(itemName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * @return A Set of all equipped set names
     */
    public Set<String> getUniqueSetNames() {
        Set<String> returnable = new HashSet<>();
        for (ItemStack itemStack : Models.Inventory.getEquippedItems()) {
            Optional<GearItem> gear = Models.Item.asWynnItem(itemStack, GearItem.class);
            if (gear.isPresent() && gear.get().getSetInfo().isPresent()) {
                returnable.add(gear.get().getSetInfo().get().name());
            }
        }
        return returnable;
    }

    public int getTrueCount(String setName) {
        int trueCount = 0;

        for (ItemStack itemStack : Models.Inventory.getEquippedItems()) {
            Optional<SetItemProperty> setItemProperty =
                    Models.Item.asWynnItemProperty(itemStack, SetItemProperty.class);
            if (setItemProperty.isEmpty() || setItemProperty.get().getSetInfo().isEmpty()) continue;
            if (setItemProperty.get().getSetInfo().get().name().equals(setName)) {
                trueCount++;
            }
        }

        return trueCount;
    }

    public boolean hasSetData() {
        return !setData.isEmpty();
    }

    private void handleSetData(Reader reader) {
        TypeToken<Map<String, RawSetInfo>> type = new TypeToken<>() {};
        Map<String, RawSetInfo> rawSets = Managers.Json.GSON.fromJson(reader, type.getType());
        rawSets.forEach((setName, rawSetInfo) -> {
            Map<Integer, SetBonus> bonuses = new HashMap<>();

            for (Map.Entry<String, RawSetBonus> entry : rawSetInfo.bonuses.entrySet()) {
                int itemCount;
                try {
                    itemCount = Integer.parseInt(entry.getKey());
                } catch (NumberFormatException e) {
                    WynntilsMod.warn("Invalid set bonus item count in set: " + setName);
                    continue;
                }

                Map<StatType, Integer> minor = new HashMap<>();
                for (Map.Entry<String, Integer> minorEntry :
                        entry.getValue().minor.entrySet()) {
                    StatType statType = Models.Stat.fromApiName(minorEntry.getKey());
                    if (statType == null) {
                        WynntilsMod.warn("Unknown stat type in set bonus: " + minorEntry.getKey());
                        continue;
                    }
                    minor.put(statType, minorEntry.getValue());
                }

                bonuses.put(itemCount, new SetBonus(entry.getValue().major(), minor));
            }

            // Ensure each item count has a value
            int max = bonuses.keySet().stream().max(Integer::compareTo).orElse(0);
            for (int i = 1; i <= max; i++) {
                bonuses.putIfAbsent(i, SetBonus.EMPTY);
            }

            setData.put(setName, new SetInfo(setName, setName.replace("'", ""), bonuses, rawSetInfo.parts));
        });
    }

    private record RawSetInfo(Map<String, RawSetBonus> bonuses, List<String> parts) {}

    private record RawSetBonus(List<String> major, Map<String, Integer> minor) {}
}
