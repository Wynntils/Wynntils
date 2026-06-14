
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.util.Collection;
import java.util.List;

public class BrokenMantleAbility extends CastedAbilityType {
    private static final ClassType CLASS_TYPE = ClassType.WARRIOR;
    private static final SpellType SPELL_TYPE = SpellType.WAR_SCREAM;
    private static final String NAME = "Broken Mantle";
    private static final String GROUP = "Broken Mantle";

    private final MantleAbility mantleAbility;

    public BrokenMantleAbility(MantleAbility mantleAbility) {
        super(CLASS_TYPE, SPELL_TYPE, null, NAME);
        this.mantleAbility = mantleAbility;
    }

    @Override
    public boolean verifyCustomModelData(List<Float> customModelData) {
        if (customModelData.isEmpty()) return false;

        return customModelData.stream()
                .allMatch(f -> Services.CustomModel.getGroup(f)
                        .map(g -> g.equals(GROUP))
                        .orElse(false));
    }

    @Override
    public boolean allowsOutOfWindowSpawn(List<Float> modelIds) {
        if (!mantleAbility.isActive()) return false;

        return verifyCustomModelData(modelIds);
    }

    @Override
    public void onMatched(int entityId, List<Float> modelIds) {
        // No registration delay needed - broken mantles spawn as a single,
        // immediate event triggered by taking damage, not a multi-piece cast.
        entityIds.add(entityId);
    }

    @Override
    public void onEntityRemoved(Collection<Integer> removedIds) {
        entityIds.removeAll(removedIds);
        if (entityIds.isEmpty()) onCleared();
    }

    @Override
    public boolean isShieldType() {
        return true;
    }
}