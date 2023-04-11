/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.abilities.type.AbilityTreeInfo;
import com.wynntils.models.character.type.ClassType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityTreeModel extends Model {
    private Map<ClassType, AbilityTreeInfo> ABILIIY_TREE_MAP = new HashMap<>();

    public AbilityTreeModel() {
        super(List.of());

        reloadData();
    }

    @Override
    public void reloadData() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_ABILITIES);
        dl.handleReader(reader -> {
            Type type = new TypeToken<HashMap<String, AbilityTreeInfo>>() {}.getType();
            Gson gson = new GsonBuilder().create();

            Map<String, AbilityTreeInfo> abilityMap = gson.fromJson(reader, type);

            Map<ClassType, AbilityTreeInfo> tempMap = new HashMap<>();

            abilityMap.forEach((key, value) -> tempMap.put(ClassType.fromName(key), value));

            ABILIIY_TREE_MAP = tempMap;
        });
    }

    public AbilityTreeInfo getAbilityTree(ClassType type) {
        return ABILIIY_TREE_MAP.get(type);
    }
}
