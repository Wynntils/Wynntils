/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.models.activities.event.ContentBookOpenEvent.OpenAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLockContentBookFeature {
    @Test
    public void blocksNormalOpeningsDuringRecentCombat() {
        for (OpenAction action :
                new OpenAction[] {OpenAction.LEFT_CLICK, OpenAction.RIGHT_CLICK, OpenAction.PROGRAMMATIC}) {
            Assertions.assertTrue(LockContentBookFeature.shouldBlockOpen(action, 0), action.name());
            Assertions.assertTrue(LockContentBookFeature.shouldBlockOpen(action, 4_999), action.name());
        }
    }

    @Test
    public void shiftAndInventoryAlwaysBypassCombatLock() {
        for (OpenAction action : new OpenAction[] {
            OpenAction.SHIFT_LEFT_CLICK,
            OpenAction.SHIFT_RIGHT_CLICK,
            OpenAction.INVENTORY_CLICK,
            OpenAction.SHIFT_INVENTORY_CLICK
        }) {
            Assertions.assertFalse(LockContentBookFeature.shouldBlockOpen(action, 0), action.name());
        }
    }

    @Test
    public void unlocksAfterFiveSecondsAndBeforeAnyDamage() {
        for (OpenAction action : OpenAction.values()) {
            Assertions.assertFalse(LockContentBookFeature.shouldBlockOpen(action, 5_000), action.name());
            Assertions.assertFalse(LockContentBookFeature.shouldBlockOpen(action, Long.MAX_VALUE), action.name());
        }
    }
}
