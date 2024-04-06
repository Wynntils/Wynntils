/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.utils.type.RangedValue;

// The range is actually possible derive from the other values, but is so commonly used
// that we cache it here as well
public record StatPossibleValues(StatType statType, RangedValue range, int baseValue, boolean isPreIdentified) {}
