/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.utils.type.CappedValue;

/**
 * Data class representing the ultimate information of a spell, including the ultimate index and progress.
 *
 * @param ultimateIndex 0-based index of the ultimate in relation the the class
 * @param progress      the progress of the ultimate, represented as a capped value where the current value is the progress and the cap is the maximum progress needed to fully charge the ultimate
 */
public record UltimateInfo(int ultimateIndex, CappedValue progress) {
    public static final UltimateInfo EMPTY = new UltimateInfo(-1, CappedValue.EMPTY);
}
