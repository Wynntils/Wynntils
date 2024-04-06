/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.profile;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryProfile {
    private int level;
    private String type;
    private String name;
    private final List<String> requirements = new ArrayList<>();

    public int getLevel() {
        return level;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<String> getRequirements() {
        return requirements;
    }
}
