/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.models.character.type.ClassType;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestQuickCastInputSuppression {
    @Test
    public void doesNotSuppressLiveAttackInputWhileSpellCasterIsIdle() {
        Assertions.assertFalse(QuickCastFeature.shouldSuppressAttackInput(false, false));
        Assertions.assertFalse(QuickCastFeature.shouldSuppressDestroyBlockInput(false, false));
        Assertions.assertFalse(QuickCastFeature.shouldSuppressMainHandInput(false, false, InteractionHand.MAIN_HAND));
    }

    @Test
    public void suppressesLiveAttackInputWhileQueuedInputsAreBeingSent() {
        Assertions.assertTrue(QuickCastFeature.shouldSuppressAttackInput(true, false));
        Assertions.assertTrue(QuickCastFeature.shouldSuppressDestroyBlockInput(true, false));
        Assertions.assertTrue(QuickCastFeature.shouldSuppressMainHandInput(true, false, InteractionHand.MAIN_HAND));
    }

    @Test
    public void doesNotSuppressOffHandUseWhileQueuedInputsAreBeingSent() {
        Assertions.assertFalse(QuickCastFeature.shouldSuppressMainHandInput(true, false, InteractionHand.OFF_HAND));
    }

    @Test
    public void suppressesNormalAttackTriggerWhenItActsAsSpellModifier() {
        Assertions.assertTrue(QuickCastFeature.isNormalAutoAttackTriggerActingAsSpellModifier(
                true, true, ClassType.MAGE, true, true, false, true, true, false));
        Assertions.assertTrue(QuickCastFeature.shouldSuppressAttackInput(false, true));
        Assertions.assertTrue(QuickCastFeature.shouldSuppressDestroyBlockInput(false, true));
    }

    @Test
    public void suppressesNormalUseTriggerForArchersWhenItActsAsSpellModifier() {
        Assertions.assertTrue(QuickCastFeature.isNormalAutoAttackTriggerActingAsSpellModifier(
                true, true, ClassType.ARCHER, true, false, true, true, true, false));
        Assertions.assertTrue(QuickCastFeature.shouldSuppressMainHandInput(false, true, InteractionHand.MAIN_HAND));
    }

    @Test
    public void doesNotTreatHeldAttackAsModifierWithoutSpellContext() {
        Assertions.assertFalse(QuickCastFeature.isNormalAutoAttackTriggerActingAsSpellModifier(
                true, true, ClassType.MAGE, true, true, false, false, false, false));
    }

    @Test
    public void doesNotTreatHeldAttackAsModifierWhenQuickCastIsNotCastable() {
        Assertions.assertFalse(QuickCastFeature.isNormalAutoAttackTriggerActingAsSpellModifier(
                true, false, ClassType.MAGE, true, true, false, true, true, false));
    }
}
