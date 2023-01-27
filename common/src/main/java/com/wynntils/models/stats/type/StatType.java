/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

// The key is strictly not necessary, but is internally useful
// The "loreName" is what is used in the json lore of other player's items
public record StatType(String key, String displayName, String apiName, String loreName, StatUnit unit) {}
