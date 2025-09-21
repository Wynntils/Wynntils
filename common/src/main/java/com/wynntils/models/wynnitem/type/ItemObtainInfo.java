/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

import java.util.Optional;

public record ItemObtainInfo(ItemObtainType sourceType, Optional<String> name) {
    public static final ItemObtainInfo UNKNOWN = new ItemObtainInfo(ItemObtainType.UNKNOWN, Optional.empty());
}
