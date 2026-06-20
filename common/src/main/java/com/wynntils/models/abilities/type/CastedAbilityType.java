/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.utils.mc.McUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public abstract class CastedAbilityType {
    private final ClassType validClass;
    private final SpellType validSpell;
    private final SpellType validUnconfirmedSpell;
    private final String name;
    private final String group;

    protected Set<Integer> entityIds = new HashSet<>();
    protected final Set<Integer> pendingEntityIds = new HashSet<>();
    private boolean registrationScheduled = false;

    protected CastedAbilityType(
            ClassType classType, SpellType spellType, SpellType unconfirmedSpellType, String name, String group) {
        this.validClass = classType;
        this.validSpell = spellType;
        this.validUnconfirmedSpell = unconfirmedSpellType;
        this.name = name;
        this.group = group;
    }

    public boolean verifyCustomModelData(List<Float> modelIds) {
        if (modelIds.isEmpty()) return false;

        return modelIds.stream()
                .allMatch(f -> Services.CustomModel.getGroup(f)
                        .map(g -> g.equals(group))
                        .orElse(false));
    }

    public String getName() {
        return name;
    }

    public boolean validSpell(SpellType spellType) {
        return validSpell != null && spellType == validSpell;
    }

    public boolean validUnconfirmedSpell(SpellType spellType) {
        return validUnconfirmedSpell != null && spellType == validUnconfirmedSpell;
    }

    public boolean validClass() {
        return Models.Character.getClassType() == validClass;
    }

    public boolean allowsOutOfWindowSpawn(List<Float> modelIds) {
        return false;
    }

    public boolean isOutsideProximity(Entity entity) {
        Player player = McUtils.player();
        Vec3 playerPos = player.position();
        Vec3 entityPos = entity.position();
        Vec3 playerVel = player.getDeltaMovement();

        double dx = entityPos.x - playerPos.x;
        double dy = entityPos.y - playerPos.y;
        double dz = entityPos.z - playerPos.z;

        double horizontalDist = Math.hypot(dx, dz);
        double verticalDist = Math.abs(dy);

        double horizontalSpeed = Math.hypot(playerVel.x, playerVel.z);
        double verticalSpeed = Math.abs(playerVel.y);

        double horizontalThreshold = 3.0 + horizontalSpeed * 4.5;
        double verticalThreshold = 4.5 + verticalSpeed * 4.5;

        return horizontalDist > horizontalThreshold || verticalDist > verticalThreshold;
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

        if (entityIds.isEmpty() && pendingEntityIds.isEmpty()) {
            onCleared();
        }
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
}
