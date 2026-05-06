/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestQuickCastMeleeInsertionState {
    @Test
    public void prependsMeleeWhenAutoInsertionIsEnabledAndNoStandaloneMeleeWasQueued() {
        QuickCastFeature.QuickCastMeleeInsertionState state = new QuickCastFeature.QuickCastMeleeInsertionState();

        Assertions.assertTrue(state.shouldPrependMelee(true, true));
        Assertions.assertFalse(state.shouldPrependMelee(false, true));
        Assertions.assertTrue(state.shouldQueueStandaloneMelee(true));
        Assertions.assertFalse(state.shouldQueueStandaloneMelee(false));
    }

    @Test
    public void doesNotPrependMeleeWhileCooldownBlocked() {
        QuickCastFeature.QuickCastMeleeInsertionState state = new QuickCastFeature.QuickCastMeleeInsertionState();

        Assertions.assertFalse(state.shouldPrependMelee(true, false));
    }

    @Test
    public void standaloneMeleeSatisfiesOnlyTheNextSpell() {
        QuickCastFeature.QuickCastMeleeInsertionState state = new QuickCastFeature.QuickCastMeleeInsertionState();

        state.onStandaloneMeleeQueued();

        Assertions.assertFalse(state.shouldPrependMelee(true, true));

        state.onSpellQueued();

        Assertions.assertTrue(state.shouldPrependMelee(true, true));
    }

    @Test
    public void standaloneMeleeCreditPersistsWhileIdleUntilNextSpellConsumesIt() {
        QuickCastFeature.QuickCastMeleeInsertionState state = new QuickCastFeature.QuickCastMeleeInsertionState();

        state.onStandaloneMeleeQueued();

        Assertions.assertFalse(state.shouldPrependMelee(true, true));

        state.onSpellQueued();

        Assertions.assertTrue(state.shouldPrependMelee(true, true));
    }

    @Test
    public void bufferedStandaloneMeleePersistsUntilSpellSelectionClearsIt() {
        QuickCastFeature.QuickCastMeleeInsertionState state = new QuickCastFeature.QuickCastMeleeInsertionState();

        state.onStandaloneMeleeCooldownBlocked();

        Assertions.assertTrue(state.hasBufferedStandaloneMelee());

        state.onSpellSelected();

        Assertions.assertFalse(state.hasBufferedStandaloneMelee());
    }

    @Test
    public void pendingStandaloneMeleePressPersistsUntilSpellSelectionClearsIt() {
        QuickCastFeature.QuickCastMeleeInsertionState state = new QuickCastFeature.QuickCastMeleeInsertionState();

        state.onStandaloneMeleePressed();

        Assertions.assertTrue(state.hasPendingStandaloneMeleePress());

        state.onSpellSelected();

        Assertions.assertFalse(state.hasPendingStandaloneMeleePress());
    }

    @Test
    public void queuedStandaloneMeleeClearsBufferedRetryAndGrantsSpellCredit() {
        QuickCastFeature.QuickCastMeleeInsertionState state = new QuickCastFeature.QuickCastMeleeInsertionState();

        state.onStandaloneMeleePressed();
        state.onStandaloneMeleeCooldownBlocked();
        state.onStandaloneMeleeQueued();

        Assertions.assertFalse(state.hasBufferedStandaloneMelee());
        Assertions.assertFalse(state.hasPendingStandaloneMeleePress());
        Assertions.assertFalse(state.shouldPrependMelee(true, true));
    }

    @Test
    public void clearRemovesStandaloneMeleeCredit() {
        QuickCastFeature.QuickCastMeleeInsertionState state = new QuickCastFeature.QuickCastMeleeInsertionState();

        state.onStandaloneMeleePressed();
        state.onStandaloneMeleeQueued();
        state.onStandaloneMeleeCooldownBlocked();
        state.clear();

        Assertions.assertTrue(state.shouldPrependMelee(true, true));
        Assertions.assertFalse(state.hasBufferedStandaloneMelee());
        Assertions.assertFalse(state.hasPendingStandaloneMeleePress());
    }
}
