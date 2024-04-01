/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.gear.type.SetSlot;
import com.wynntils.models.stats.type.StatType;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetModel extends Model {
    // Stored as a map for quick lookup <name, SetInfo>
    private final Map<String, SetInfo> setData = new HashMap<>();
    private final Map<SetSlot, SetInstance> setInstances = new EnumMap<>(SetSlot.class);

    public SetModel() {
        super(List.of());
        loadSetData();
    }

    public SetInfo getSetInfoForId(String setId) {
        return setData.getOrDefault(setId, null);
    }

    public SetInfo getSetInfoForItem(String itemName) {
        return getSetInfoForId(getSetName(itemName));
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

    public void updateSetInstance(SetSlot slot, SetInstance instance) {
        setInstances.put(slot, instance);
    }

    public SetInstance getSetInstance(SetSlot slot) {
        return setInstances.get(slot);
    }

    private void loadSetData() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_ITEM_SETS);
        dl.handleReader(reader -> {
            Map<String, RawSetInfo> rawSets = WynntilsMod.GSON.fromJson(reader, new TypeToken<Map<String, RawSetInfo>>() {}.getType());
            rawSets.forEach((setName, rawSetInfo) -> {

                List<Map<StatType, Integer>> bonuses = rawSetInfo.bonuses.stream()
                        .map(bonusPair -> {
                            Map<StatType, Integer> bonusMap = new HashMap<>();
                            bonusPair.forEach((statName, statValue) -> {
                                StatType statType = Models.Stat.fromApiName(statName);
                                if (statType == null) {
                                    WynntilsMod.warn("Unknown stat type: " + statName);
                                }
                                bonusMap.put(statType, statValue);
                            });
                            return bonusMap;
                        })
                        .toList();

                setData.put(setName, new SetInfo(setName, bonuses, rawSetInfo.items));
                System.out.println("Loaded set: " + setName + " (" + rawSetInfo.items.size() + " items)");
            });
        });
    }

    private static class RawSetInfo {
        public List<Map<String, Integer>> bonuses;
        public List<String> items;
    }
}
