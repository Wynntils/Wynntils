/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

import com.wynntils.models.rewards.type.TomeType;

public record SavableTome(TomeType type, String name, String encoded) {}
