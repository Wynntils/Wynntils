/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.utils.mc.McUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.Entity;

public abstract class CastedAbilityType {
    private final ClassType validClass;
    private final SpellType validSpell;
    private final SpellType validPartialSpell;
    private final String name;

    protected Set<Integer> entityIds = new HashSet<>();
    protected final Set<Integer> pendingEntityIds = new HashSet<>();
    private boolean registrationScheduled = false;

    protected CastedAbilityType(ClassType classType, SpellType spellType, SpellType partialSpellType, String name) {
        this.validClass = classType;
        this.validSpell = spellType;
        this.validPartialSpell = partialSpellType;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean validSpell(SpellType spellType) {
        return spellType == validSpell;
    }

    public boolean validPartialSpell(SpellType spellType) {
        return validPartialSpell != null && spellType == validPartialSpell;
    }

    public boolean validClass() {
        return Models.Character.getClassType() == validClass;
    }

    public boolean allowsOutOfWindowSpawn(List<Float> modelIds) {
        return false;
    }

    public abstract boolean verifyCustomModelData(List<Float> customModelData);

    public boolean isOutsideProximity(Entity entity) {
        return entity.position().distanceTo(McUtils.player().position()) > 4.5;
    }

    public void onMatched(int entityId, List<Float> modelIds) {
        pendingEntityIds.add(entityId);
        scheduleRegistration();
    }

    protected void scheduleRegistration() {
        if (registrationScheduled) return;
        registrationScheduled = true;

        Managers.TickScheduler.scheduleLater(this::registerEntities, 4);
    }

    protected void registerEntities() {
        registrationScheduled = false;
        if (pendingEntityIds.isEmpty()) return;

        entityIds = new HashSet<>(pendingEntityIds);
        pendingEntityIds.clear();
    }

    public void onEntityRemoved(Collection<Integer> removedIds) {
        pendingEntityIds.removeAll(removedIds);
        entityIds.removeAll(removedIds);

        if (entityIds.isEmpty() && pendingEntityIds.isEmpty()) onCleared();
    }

    public void onCleared() {
        entityIds.clear();
        pendingEntityIds.clear();
        registrationScheduled = false;
    }

    public boolean isActive() {
        return !entityIds.isEmpty();
    }

    public int getCharge() {
        return entityIds.size();
    }

    public Set<Class<? extends CastedAbilityType>> getConflictingTypes() {
        return Set.of();
    }

    public boolean isShieldType() {
        return false;
    }
}
