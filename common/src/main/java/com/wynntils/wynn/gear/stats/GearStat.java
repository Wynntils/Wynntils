/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;

public record GearStat(String key, String displayName, String apiName, String loreName, GearStatUnit unit) {}
