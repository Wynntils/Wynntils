/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import java.util.function.Consumer;

public abstract class StatBuilder {
    public abstract void buildStats(Consumer<GearStat> callback);
}
