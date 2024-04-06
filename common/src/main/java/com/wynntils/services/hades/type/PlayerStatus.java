/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hades.type;

import com.wynntils.utils.type.CappedValue;

public record PlayerStatus(float x, float y, float z, CappedValue health, CappedValue mana) {}
