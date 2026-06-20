/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;

public class JudrajimAbility extends CastedAbilityType {
    private static final ClassType CLASS_TYPE = ClassType.MAGE;
    private static final SpellType UNCONFIRMED_SPELL_TYPE = SpellType.HEAL;
    private static final String NAME = "Judrajim";
    private static final String GROUP = "Judrajim";

    public JudrajimAbility() {
        super(CLASS_TYPE, null, UNCONFIRMED_SPELL_TYPE, NAME, GROUP);
    }

    /**
     * This needs to be scheduled later, because the removal of the model and the cooldown need to overlap.
     */
    @Override
    public void onEntityRemoved(Collection<Integer> removedIds) {
        Managers.TickScheduler.scheduleLater(() -> super.onEntityRemoved(removedIds), 20);
    }
}
