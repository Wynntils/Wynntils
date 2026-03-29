/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAutoAttackFeature {
    @Test
    public void queuesHeldAutoAttackWhenTriggerIsHeldOutsideSpellWindows() {
        Assertions.assertTrue(
                AutoAttackFeature.shouldQueueHeldAutoAttack(true, true, false, false, false, false, true));
    }

    @Test
    public void doesNotQueueHeldAutoAttackWhileQuickCastUsesTheTriggerAsModifier() {
        Assertions.assertFalse(
                AutoAttackFeature.shouldQueueHeldAutoAttack(true, true, false, true, false, false, true));
    }

    @Test
    public void doesNotQueueHeldAutoAttackInsideSpellWindow() {
        Assertions.assertFalse(
                AutoAttackFeature.shouldQueueHeldAutoAttack(true, true, false, false, false, true, true));
    }

    @Test
    public void doesNotQueueHeldAutoAttackWithoutPlayerOrWorld() {
        Assertions.assertFalse(
                AutoAttackFeature.shouldQueueHeldAutoAttack(false, true, false, false, false, false, true));
        Assertions.assertFalse(
                AutoAttackFeature.shouldQueueHeldAutoAttack(true, false, false, false, false, false, true));
    }
}
