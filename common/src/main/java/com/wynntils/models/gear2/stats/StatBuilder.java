/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear2.stats;

import com.wynntils.models.gear2.types.GearStat;
import java.util.function.Consumer;

public abstract class StatBuilder {
    public abstract void buildStats(Consumer<GearStat> callback);
}
