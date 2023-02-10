/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

public record PowderProfile(
        Powder element, int tier, int min, int max, int convertedFromNeutral, int addedDefence, int removedDefence) {}
