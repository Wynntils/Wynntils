/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.wynntils.models.players.cosmetics.CosmeticInfo;
import com.wynntils.models.players.type.AccountType;

public record WynntilsUser(AccountType accountType, CosmeticInfo cosmetics) {}
