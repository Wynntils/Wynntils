/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

public record PowderTierInfo(
        Powder element,
        int tier,
        int min,
        int max,
        int convertedFromNeutral,
        int health,
        int addedDefence,
        int removedDefence) {}
