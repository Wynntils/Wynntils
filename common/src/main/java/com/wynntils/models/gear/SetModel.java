/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SetModel extends Model {
    private final Map<String, SetData> setData = new HashMap<>();

    public SetModel() {
        super(List.of());
        loadSetData();
    }

    public SetData getSetData(String setId) {
        return setData.get(setId);
    }

    private void loadSetData() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_ITEM_SETS);
        dl.handleReader(reader -> {
            setData.putAll(WynntilsMod.GSON.fromJson(reader, new TypeToken<Map<String, SetData>>() {}.getType()));
        });
    }

    public static class SetData {
        private List<Map<String, Integer>> bonuses;
        private List<String> items;

        public List<Map<String, Integer>> getBonuses() {
            return Collections.unmodifiableList(bonuses);
        }

        public List<String> getItems() {
            return Collections.unmodifiableList(items);
        }
    }
}
