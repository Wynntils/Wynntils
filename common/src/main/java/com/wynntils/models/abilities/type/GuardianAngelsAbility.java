/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class GuardianAngelsAbility extends CastedAbilityType {
    private static final ClassType CLASS_TYPE = ClassType.ARCHER;
    private static final SpellType SPELL_TYPE = SpellType.ARROW_SHIELD;
    private static final String NAME = "Guardian Angels";
    private static final String GROUP = "Guardian Angels";
    private static final String ULT_GROUP = "Angelic Ascension Guardian Angels";

    private String activeGroup;
    private String pendingGroup;

    public GuardianAngelsAbility() {
        super(CLASS_TYPE, SPELL_TYPE, null, NAME);
    }

    @Override
    public boolean verifyCustomModelData(List<Float> modelIds) {
        if (modelIds.isEmpty()) return false;

        Set<String> groups = modelIds.stream()
                .map(f -> Services.CustomModel.getGroup(f).orElse(null))
                .collect(Collectors.toSet());

        return groups.size() == 1 && (groups.contains(GROUP) || groups.contains(ULT_GROUP));
    }

    @Override
    public boolean allowsOutOfWindowSpawn(List<Float> modelIds) {
        if (!isActive()) return false;
        if (modelIds.isEmpty()) return false;

        Set<String> groups = modelIds.stream()
                .map(f -> Services.CustomModel.getGroup(f).orElse(null))
                .collect(Collectors.toSet());

        if (groups.size() != 1) return false;

        String group = groups.iterator().next();
        boolean isGaFamily = GROUP.equals(group) || ULT_GROUP.equals(group);
        return isGaFamily && !group.equals(activeGroup);
    }

    @Override
    public void onMatched(int entityId, List<Float> modelIds) {
        pendingEntityIds.add(entityId);
        pendingGroup = modelIds.stream()
                .map(f -> Services.CustomModel.getGroup(f).orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        scheduleRegistration();
    }

    @Override
    protected void registerEntities() {
        if (pendingEntityIds.isEmpty()) {
            super.registerEntities();
            return;
        }

        activeGroup = pendingGroup;
        super.registerEntities();
    }

    public boolean isUltimate() {
        return ULT_GROUP.equals(activeGroup);
    }

    @Override
    public Set<Class<? extends CastedAbilityType>> getConflictingTypes() {
        return Set.of(ArrowShieldAbility.class);
    }

    @Override
    public boolean isShieldType() {
        return true;
    }
}
