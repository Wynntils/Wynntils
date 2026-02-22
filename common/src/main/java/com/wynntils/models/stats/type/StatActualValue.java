/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

public record StatActualValue(StatType statType, int value, boolean perfectInternalRoll) {}
