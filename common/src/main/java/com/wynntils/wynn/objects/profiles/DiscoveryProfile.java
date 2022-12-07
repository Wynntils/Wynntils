/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryProfile {
    int level;
    String type;
    String name;
    List<String> requirements = new ArrayList<>();

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
