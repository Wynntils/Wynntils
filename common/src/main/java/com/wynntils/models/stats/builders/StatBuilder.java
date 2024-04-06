/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.stats.type.StatType;
import java.util.function.Consumer;

public abstract class StatBuilder<T extends StatType> {
    public abstract void buildStats(Consumer<T> callback);
}
