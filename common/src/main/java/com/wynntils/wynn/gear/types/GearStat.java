/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.types;

// The key is strictly not necessary, but is internally useful
// The "loreName" is what is used in the json lore of other player's items
public record GearStat(String key, String displayName, String apiName, String loreName, GearStatUnit unit) {}
