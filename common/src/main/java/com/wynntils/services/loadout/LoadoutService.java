package com.wynntils.services.loadout;

import com.wynntils.core.components.Service;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.models.character.type.SavableTomeSet;
import com.wynntils.services.loadout.type.LoadoutType;
import com.wynntils.services.loadout.type.Loadout;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LoadoutService extends Service {

    @Persisted
    public final Storage<Map<String, Loadout>> loadouts = new Storage<>(new TreeMap<>());

    public LoadoutService() {
        super(List.of());
    }

    public Map<String, Loadout> getLoadouts() {
        return loadouts.get();
    }

    public Loadout getLoadout(String name) {
        return loadouts.get().get(name);
    }

    public boolean hasLoadout(String name) {
        return loadouts.get().containsKey(name);
    }

    public void deleteLoadout(String name) {
        loadouts.get().remove(name);
        loadouts.touched();
    }

    public void saveLoadout(String name, Loadout loadout) {
        loadouts.get().put(name, loadout);
        loadouts.touched();
    }

    public SavableAbilityTree getAbilityTreeLoadout(String name) {
        Loadout loadout = getLoadout(name);
        return loadout != null ? loadout.abilityTree() : null;
    }

    public SavableAspectSet getAspectLoadout(String name) {
        Loadout loadout = getLoadout(name);
        return loadout != null ? loadout.aspects() : null;
    }

    public SavableSkillPointSet getSkillPointLoadout(String name) {
        Loadout loadout = getLoadout(name);
        return loadout != null ? loadout.skillPoints() : null;
    }

    public SavableTomeSet getTomeLoadout(String name) {
        Loadout loadout = getLoadout(name);
        return loadout != null ? loadout.tomes() : null;
    }

    public void saveAbilityTreeLoadout(String name, SavableAbilityTree abilityTree) {
        Loadout existing = getLoadout(name);
        Loadout updated = new Loadout(
                name,
                determineType(existing, LoadoutType.ABILITY_TREE),
                existing != null ? existing.skillPoints() : null,
                existing != null ? existing.tomes() : null,
                abilityTree,
                existing != null ? existing.aspects() : null,
                existing != null && existing.favourited());
        saveLoadout(name, updated);
    }

    public void saveAspectLoadout(String name, SavableAspectSet aspects) {
        Loadout existing = getLoadout(name);
        Loadout updated = new Loadout(
                name,
                determineType(existing, LoadoutType.ASPECT),
                existing != null ? existing.skillPoints() : null,
                existing != null ? existing.tomes() : null,
                existing != null ? existing.abilityTree() : null,
                aspects,
                existing != null && existing.favourited());
        saveLoadout(name, updated);
    }

    public void saveSkillPointLoadoutAndTomes(String name, SavableSkillPointSet skillPoints, SavableTomeSet tomes) {
        Loadout existing = getLoadout(name);
        Loadout updated = new Loadout(
                name,
                determineType(existing, LoadoutType.SKILL_POINT),
                skillPoints,
                tomes,
                existing != null ? existing.abilityTree() : null,
                existing != null ? existing.aspects() : null,
                existing != null && existing.favourited());
        saveLoadout(name, updated);
    }

    public void saveBuildLoadout(String name, SavableSkillPointSet skillPoints, SavableTomeSet tomes,
                                 SavableAbilityTree abilityTree, SavableAspectSet aspects) {
        Loadout existing = getLoadout(name);
        Loadout updated = new Loadout(
                name,
                LoadoutType.BUILD,
                skillPoints,
                tomes,
                abilityTree,
                aspects,
                existing != null && existing.favourited());
        saveLoadout(name, updated);
    }

    public void setFavourited(String name, boolean favourited) {
        Loadout existing = getLoadout(name);
        if (existing == null) return;
        Loadout updated = new Loadout(
                name,
                existing.type(),
                existing.skillPoints(),
                existing.tomes(),
                existing.abilityTree(),
                existing.aspects(),
                favourited);
        saveLoadout(name, updated);
    }

    private LoadoutType determineType(Loadout existing, LoadoutType added) {
        if (existing == null) return added;

        boolean hasSkillPoints = existing.skillPoints() != null || added == LoadoutType.SKILL_POINT;
        boolean hasAbilityTree = existing.abilityTree() != null || added == LoadoutType.ABILITY_TREE;
        boolean hasAspects = existing.aspects() != null || added == LoadoutType.ASPECT;

        int count = 0;
        if (hasSkillPoints) count++;
        if (hasAbilityTree) count++;
        if (hasAspects) count++;

        return count > 1 ? LoadoutType.BUILD : added;
    }
}