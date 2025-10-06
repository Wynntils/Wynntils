/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gambits.type;

public enum GambitStatus {
    ENABLED,
    DISABLED,
    // PLAYER_READY should be a gambit state, because we can't get the status of the gambit from the lore when the
    // player is ready
    PLAYER_READY
}
