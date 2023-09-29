/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.crowdsource.datatype;

import com.google.common.collect.ComparisonChain;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.utils.mc.type.Location;

public record LootrunTaskLocation(LootrunLocation region, Location location)
        implements Comparable<LootrunTaskLocation> {
    @Override
    public int compareTo(LootrunTaskLocation other) {
        return ComparisonChain.start()
                .compare(region, other.region)
                .compare(location, other.location)
                .result();
    }
}
