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
import com.wynntils.models.stats.type.StatType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetModel extends Model {
    private final Map<String, SetInfo> setData = new HashMap<>();

    public SetModel() {
        super(List.of());
        loadSetData();
    }

    public SetInfo getSetData(String setId) {
        return setData.get(setId);
    }

    private void loadSetData() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_ITEM_SETS);
        dl.handleReader(reader -> {
            Map<String, Map<String, List<?>>> tempData =
                    WynntilsMod.GSON.fromJson(reader, new TypeToken<Map<String, Map<String, List<?>>>>() {}.getType());
            tempData.forEach((key, value) -> {
                List<Map<String, Double>> rawBonuses = (List<Map<String, Double>>) value.get("bonuses");
                List<String> items = (List<String>) value.get("items");

                List<Map<StatType, Integer>> bonuses = rawBonuses.stream()
                        .map(bonus -> {
                            Map<StatType, Integer> bonusMap = new HashMap<>();
                            bonus.forEach((statName, statValue) -> {
                                StatType statType = Models.Stat.fromApiName(statName);
                                if (statType == null) {
                                    WynntilsMod.warn("Unknown stat type: " + statName);
                                }
                                bonusMap.put(statType, statValue.intValue());
                            });
                            return bonusMap;
                        })
                        .toList();

                setData.put(key, new SetInfo(bonuses, items));
            });
        });
    }

    public static final class SetInfo {
        private final List<Map<StatType, Integer>> bonuses;
        private final List<String> items;

        private SetInfo(List<Map<StatType, Integer>> bonuses, List<String> items) {
            this.bonuses = bonuses;
            this.items = items;
        }

        public List<Map<StatType, Integer>> getBonuses() {
            return Collections.unmodifiableList(bonuses);
        }

        /**
         * @param numberOfItems The number of items equipped to get the set bonus for
         * @return A map of stat names to the bonus value for that stat
         */
        public Map<StatType, Integer> getBonusForItems(int numberOfItems) {
            return bonuses.get(numberOfItems - 1);
        }

        public List<String> getItems() {
            return Collections.unmodifiableList(items);
        }

        @Override
        public String toString() {
            return "SetData{" + "bonuses=" + bonuses + ", items=" + items + '}';
        }
    }
}
