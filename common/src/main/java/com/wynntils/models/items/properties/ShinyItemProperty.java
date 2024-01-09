/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.models.stats.type.ShinyStat;
import java.util.Optional;

public interface ShinyItemProperty {
    Optional<ShinyStat> getShinyStat();
}
