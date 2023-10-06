/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.type;

import com.google.common.collect.ComparisonChain;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.mc.type.Location;

public abstract class LabelInfo implements Comparable<LabelInfo> {
    protected final StyledText label;
    protected final String name;
    protected final Location location;

    protected LabelInfo(StyledText label, Location location) {
        this.label = label;
        this.name = label.getStringWithoutFormatting();
        this.location = location;
    }

    protected LabelInfo(StyledText label, String name, Location location) {
        this.label = label;
        this.name = name;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(LabelInfo other) {
        return ComparisonChain.start()
                .compare(name, other.name)
                .compare(location, other.location)
                .result();
    }
}
