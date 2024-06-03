/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.models.abilitytree.parser.AbilityTreeParser;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeInstance;
import com.wynntils.models.abilitytree.type.AbilityTreeQueryState;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.screens.abilities.CustomAbilityTreeScreen;
import com.wynntils.utils.mc.McUtils;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AbilityTreeModel extends Model {
    public static final int ABILITY_TREE_PAGES = 7;
    public static final AbilityTreeParser ABILITY_TREE_PARSER = new AbilityTreeParser();
    public static final AbilityTreeContainerQueries ABILITY_TREE_CONTAINER_QUERIES = new AbilityTreeContainerQueries();

    // Ability Tree Infos sourced from our data (remote)
    private Map<ClassType, AbilityTreeInfo> abilityTreeMap = new EnumMap<>(ClassType.class);

    // Parsed Ability Tree Instance and State
    private AbilityTreeInstance abilityTreeInstance = null;
    private AbilityTreeQueryState abilityTreeQueryState = null;

    public AbilityTreeModel() {
        super(List.of());

        reloadData();
    }

    @Override
    public void reloadData() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_ABILITIES);
        dl.handleReader(reader -> {
            Type type = new TypeToken<HashMap<String, AbilityTreeInfo>>() {}.getType();

            Map<String, AbilityTreeInfo> abilityMap = Managers.Json.GSON.fromJson(reader, type);

            Map<ClassType, AbilityTreeInfo> tempMap = new EnumMap<>(ClassType.class);

            abilityMap.forEach((key, value) -> tempMap.put(ClassType.fromName(key), value));

            abilityTreeMap = tempMap;
        });
    }

    @SubscribeEvent
    public void onScreenClosed(ScreenClosedEvent event) {
        if (!(event.getScreen() instanceof CustomAbilityTreeScreen)) return;

        abilityTreeInstance = null;
        abilityTreeQueryState = null;
    }

    public void openCustomAbilityTreeScreen() {
        if (abilityTreeInstance != null || abilityTreeQueryState != null) {
            WynntilsMod.warn("Opened an ability tree screen while one was already open. This should not happen.");
            // try continuing anyway
        }

        abilityTreeInstance = null;
        abilityTreeQueryState = null;

        // Check if the player has a class parsed
        if (Models.Character.getClassType() == ClassType.NONE) {
            setAbilityTreeQueryState(AbilityTreeQueryState.ERROR_CLASS_NOT_PARSED);
        }

        // Check if we have the ability tree info for the class
        if (Models.Character.hasCharacter() && !abilityTreeMap.containsKey(Models.Character.getClassType())) {
            setAbilityTreeQueryState(AbilityTreeQueryState.ERROR_NO_CLASS_DATA);
        }

        // Start parsing the ability tree instance, if there are no errors
        if (abilityTreeQueryState == null) {
            setAbilityTreeQueryState(AbilityTreeQueryState.PARSING);
            ABILITY_TREE_CONTAINER_QUERIES.parseAbilityTree(abilityTreeMap.get(Models.Character.getClassType()));
        }

        // Open the custom ability tree screen
        McUtils.mc().setScreen(new CustomAbilityTreeScreen());
    }

    public void setAbilityTreeInstance(AbilityTreeInfo abilityTreeInfo, AbilityTreeInstance abilityTreeInstance) {
        if (abilityTreeInfo == null || abilityTreeInstance == null) {
            // We have an error, set the state to error
            setAbilityTreeQueryState(AbilityTreeQueryState.ERROR_PARSING_INSTANCE);
            return;
        }

        // Validate that the ability tree instance matches the ability tree info
        if (!validateAbilityTreeInstance(abilityTreeInfo, abilityTreeInstance)) {
            // We have an error, set the state to error
            setAbilityTreeQueryState(AbilityTreeQueryState.ERROR_API_INFO_OUTDATED);
            return;
        }

        this.abilityTreeInstance = abilityTreeInstance;
        setAbilityTreeQueryState(AbilityTreeQueryState.PARSED);
    }

    public Optional<AbilityTreeInfo> getAbilityTreeInfo() {
        return Optional.ofNullable(abilityTreeMap.get(Models.Character.getClassType()));
    }

    public Optional<AbilityTreeInstance> getAbilityTreeInstance() {
        return Optional.ofNullable(abilityTreeInstance);
    }

    public AbilityTreeQueryState getAbilityTreeQueryState() {
        return abilityTreeQueryState;
    }

    private void setAbilityTreeQueryState(AbilityTreeQueryState abilityTreeQueryState) {
        this.abilityTreeQueryState = abilityTreeQueryState;

        if (McUtils.mc().screen instanceof CustomAbilityTreeScreen abilityTreeScreen) {
            abilityTreeScreen.onAbilityTreeQueryStateChanged(abilityTreeQueryState);
        }
    }

    private boolean validateAbilityTreeInstance(
            AbilityTreeInfo abilityTreeInfo, AbilityTreeInstance abilityTreeInstance) {
        // Firstly, check node counts
        if (abilityTreeInfo.nodes().size() != abilityTreeInstance.nodes().size()) {
            return false;
        }

        // Secondly, check if all nodes can be found in the instance
        for (AbilityTreeSkillNode node : abilityTreeInfo.nodes()) {
            if (!abilityTreeInstance.nodes().containsKey(node)) {
                return false;
            }
        }

        return true;
    }
}
