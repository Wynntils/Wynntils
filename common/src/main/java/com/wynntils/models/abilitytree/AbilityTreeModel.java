/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.abilitytree.parser.AbilityTreeParser;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeState;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ParsedAbilityTree;
import com.wynntils.models.character.type.ClassType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AbilityTreeModel extends Model {
    public static final int ABILITY_TREE_PAGES = 7;
    public static final AbilityTreeParser ABILITY_TREE_PARSER = new AbilityTreeParser();
    public static final AbilityTreeContainerQueries ABILITY_TREE_CONTAINER_QUERIES = new AbilityTreeContainerQueries();

    private Map<ClassType, AbilityTreeInfo> abilityTreeMap = new HashMap<>();
    private ParsedAbilityTree currentAbilityTree;

    public AbilityTreeModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_ABILITIES).handleReader(reader -> {
            Type type = new TypeToken<HashMap<String, AbilityTreeInfo>>() {}.getType();
            Gson gson = new GsonBuilder().create();

            Map<String, AbilityTreeInfo> abilityMap = gson.fromJson(reader, type);

            Map<ClassType, AbilityTreeInfo> tempMap = new HashMap<>();

            abilityMap.forEach((key, value) -> tempMap.put(ClassType.fromName(key), value));

            abilityTreeMap = tempMap;
        });
    }

    public void setCurrentAbilityTree(ParsedAbilityTree currentAbilityTree) {
        this.currentAbilityTree = currentAbilityTree;
    }

    public AbilityTreeNodeState getNodeState(AbilityTreeSkillNode node) {
        if (currentAbilityTree == null) {
            return AbilityTreeNodeState.LOCKED;
        }

        return currentAbilityTree.nodes().keySet().stream()
                .filter(n -> n.equals(node))
                .map(currentAbilityTree.nodes()::get)
                .findFirst()
                .orElse(AbilityTreeNodeState.LOCKED);
    }

    public AbilityTreeInfo getAbilityTree(ClassType type) {
        return abilityTreeMap.get(type);
    }
}
