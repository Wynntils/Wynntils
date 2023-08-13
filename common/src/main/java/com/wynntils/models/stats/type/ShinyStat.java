/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.utils.type.Pair;
import java.util.Optional;

public class ShinyStat {

    private Pair<String, Long> valuePair;

    public ShinyStat(Optional<Pair<String, Long>> shinyStat) {
        this.valuePair = shinyStat.orElse(null);
    }

    public void update(String name, long value) {
        this.valuePair = Pair.of(name, value);
    }

    public boolean isStatPresent() {
        return valuePair != null;
    }

    public String getName() {
        return this.valuePair.key();
    }
}
