/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.properties.ClassableItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.models.spells.type.CombatClickType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

final class QueuedMeleeScheduler {
    private static final List<CombatClickType> MELEE_SEQUENCE = List.of(CombatClickType.MELEE);

    static boolean canHandleCombatInput() {
        return McUtils.player() != null && Models.WorldState.onWorld() && !Models.WorldState.inCharacterWardrobe();
    }

    static boolean canQueueCombatInput(boolean checkValidWeapon) {
        return canHandleCombatInput() && resolveWeaponContext(checkValidWeapon).valid();
    }

    static boolean isMeleeReady() {
        CappedValue cooldown =
                Models.CharacterStats.getItemCooldownTicks(McUtils.player().getItemInHand(InteractionHand.MAIN_HAND));
        return cooldown.current() <= 0;
    }

    static MeleeQueueResult queueCurrentMelee(
            int leftDelayMs, int rightDelayMs, boolean adaptiveLagCorrectionEnabled, boolean checkValidWeapon) {
        if (!canHandleCombatInput()) return MeleeQueueResult.NOT_QUEUED;
        if (!isMeleeReady()) return MeleeQueueResult.BLOCKED_BY_COOLDOWN;

        WeaponContext weaponContext = resolveWeaponContext(checkValidWeapon);
        if (!weaponContext.valid()) return MeleeQueueResult.NOT_QUEUED;

        return queueCurrentMelee(weaponContext, leftDelayMs, rightDelayMs, adaptiveLagCorrectionEnabled);
    }

    static MeleeQueueResult queueCurrentMelee(
            WeaponContext weaponContext, int leftDelayMs, int rightDelayMs, boolean adaptiveLagCorrectionEnabled) {
        if (!weaponContext.valid()) return MeleeQueueResult.NOT_QUEUED;

        return Models.SpellCaster.queueClicks(
                        MELEE_SEQUENCE,
                        weaponContext.isArcher(),
                        Math.max(leftDelayMs, 0),
                        Math.max(rightDelayMs, 0),
                        0,
                        adaptiveLagCorrectionEnabled)
                ? MeleeQueueResult.QUEUED
                : MeleeQueueResult.NOT_QUEUED;
    }

    static WeaponContext resolveWeaponContext(boolean checkValidWeapon) {
        boolean isArcher = Models.Character.getClassType() == ClassType.ARCHER;
        if (!checkValidWeapon) {
            return WeaponContext.valid(isArcher);
        }

        ItemStack heldItem = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
        if (!ItemUtils.isWeapon(heldItem)) {
            return WeaponContext.invalid(InvalidWeaponReason.NOT_A_WEAPON);
        }

        Optional<ClassableItemProperty> classItemPropOpt =
                Models.Item.asWynnItemProperty(heldItem, ClassableItemProperty.class);
        if (classItemPropOpt.isEmpty()) {
            return WeaponContext.invalid(InvalidWeaponReason.NOT_A_WEAPON);
        }

        isArcher = classItemPropOpt.get().getRequiredClass() == ClassType.ARCHER;

        Optional<RequirementItemProperty> reqItemPropOpt =
                Models.Item.asWynnItemProperty(heldItem, RequirementItemProperty.class);
        if (reqItemPropOpt.isPresent() && !reqItemPropOpt.get().meetsActualRequirements()) {
            return WeaponContext.invalid(InvalidWeaponReason.REQUIREMENTS_UNMET);
        }

        return WeaponContext.valid(isArcher);
    }

    enum MeleeQueueResult {
        QUEUED,
        BLOCKED_BY_COOLDOWN,
        NOT_QUEUED
    }

    enum InvalidWeaponReason {
        NOT_A_WEAPON,
        REQUIREMENTS_UNMET
    }

    record WeaponContext(boolean valid, boolean isArcher, InvalidWeaponReason invalidWeaponReason) {
        private static WeaponContext valid(boolean isArcher) {
            return new WeaponContext(true, isArcher, null);
        }

        private static WeaponContext invalid(InvalidWeaponReason invalidWeaponReason) {
            return new WeaponContext(false, false, invalidWeaponReason);
        }
    }
}
