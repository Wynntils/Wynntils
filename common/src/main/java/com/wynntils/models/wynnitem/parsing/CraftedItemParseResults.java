/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.wynntils.utils.type.CappedValue;

public record CraftedItemParseResults(String name, int effectStrength, CappedValue uses) {}
