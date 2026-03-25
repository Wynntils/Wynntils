/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

final class QuickCastMeleeInsertionState {
    private boolean standaloneMeleeQueued;
    private boolean bufferedStandaloneMelee;
    private boolean pendingStandaloneMeleePress;

    boolean shouldPrependMelee(boolean autoInsertionEnabled, boolean meleeReady) {
        return autoInsertionEnabled && meleeReady && !standaloneMeleeQueued;
    }

    boolean hasBufferedStandaloneMelee() {
        return bufferedStandaloneMelee;
    }

    boolean hasPendingStandaloneMeleePress() {
        return pendingStandaloneMeleePress;
    }

    boolean shouldQueueStandaloneMelee(boolean meleeReady) {
        return meleeReady;
    }

    void onStandaloneMeleePressed() {
        pendingStandaloneMeleePress = true;
    }

    void onStandaloneMeleeCooldownBlocked() {
        bufferedStandaloneMelee = true;
        pendingStandaloneMeleePress = false;
    }

    void onStandaloneMeleeQueued() {
        standaloneMeleeQueued = true;
        bufferedStandaloneMelee = false;
        pendingStandaloneMeleePress = false;
    }

    void onStandaloneMeleeRejected() {
        pendingStandaloneMeleePress = false;
    }

    void onSpellSelected() {
        bufferedStandaloneMelee = false;
        pendingStandaloneMeleePress = false;
    }

    void onSpellQueued() {
        standaloneMeleeQueued = false;
        bufferedStandaloneMelee = false;
        pendingStandaloneMeleePress = false;
    }

    void clear() {
        standaloneMeleeQueued = false;
        bufferedStandaloneMelee = false;
        pendingStandaloneMeleePress = false;
    }
}
