/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.bossbar.type;

import com.wynntils.utils.type.CappedValue;

public record BossBarProgress(CappedValue value, float progress) {}
