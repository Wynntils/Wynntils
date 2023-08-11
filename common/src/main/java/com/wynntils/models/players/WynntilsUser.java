/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.wynntils.models.players.type.AccountType;
import com.wynntils.models.players.type.CosmeticInfo;

public record WynntilsUser(AccountType accountType, CosmeticInfo cosmetics) {}
