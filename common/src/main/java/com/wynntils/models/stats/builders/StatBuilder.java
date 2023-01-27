/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.stats.type.StatType;
import java.util.function.Consumer;

public abstract class StatBuilder {
    public abstract void buildStats(Consumer<StatType> callback);
}
