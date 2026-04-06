/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.models.character.type.ClassType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestQuickCastAutoInsertTrigger {
    @Test
    public void quickCastMeleeKeyTriggersAutoInsertionWithoutAutoAttack() {
        Assertions.assertTrue(QuickCastFeature.isAutoInsertMeleeTriggerHeld(ClassType.MAGE, false, true, false, false));
        Assertions.assertTrue(
                QuickCastFeature.isAutoInsertMeleeTriggerHeld(ClassType.ARCHER, false, true, false, false));
    }

    @Test
    public void nonArchersUseNormalAttackKeyOnlyWhenAutoAttackIsEnabled() {
        Assertions.assertTrue(QuickCastFeature.isAutoInsertMeleeTriggerHeld(ClassType.MAGE, true, false, true, false));
        Assertions.assertFalse(
                QuickCastFeature.isAutoInsertMeleeTriggerHeld(ClassType.MAGE, false, false, true, false));
        Assertions.assertFalse(QuickCastFeature.isAutoInsertMeleeTriggerHeld(ClassType.MAGE, true, false, false, true));
    }

    @Test
    public void archersUseNormalUseKeyOnlyWhenAutoAttackIsEnabled() {
        Assertions.assertTrue(
                QuickCastFeature.isAutoInsertMeleeTriggerHeld(ClassType.ARCHER, true, false, false, true));
        Assertions.assertFalse(
                QuickCastFeature.isAutoInsertMeleeTriggerHeld(ClassType.ARCHER, false, false, false, true));
        Assertions.assertFalse(
                QuickCastFeature.isAutoInsertMeleeTriggerHeld(ClassType.ARCHER, true, false, true, false));
    }

    @Test
    public void deferredStandaloneMeleeWaitsForReleaseWhenRepeatIsDisabled() {
        Assertions.assertFalse(QuickCastFeature.shouldQueueDeferredStandaloneMelee(true, true, false));
        Assertions.assertFalse(QuickCastFeature.shouldQueueDeferredStandaloneMelee(true, false, false));
        Assertions.assertTrue(QuickCastFeature.shouldQueueDeferredStandaloneMelee(false, false, false));
    }

    @Test
    public void deferredStandaloneMeleeQueuesOnHeldRepeatAfterInitialPress() {
        Assertions.assertFalse(QuickCastFeature.shouldQueueDeferredStandaloneMelee(true, true, true));
        Assertions.assertTrue(QuickCastFeature.shouldQueueDeferredStandaloneMelee(true, false, true));
    }
}
