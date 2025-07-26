/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena.type;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public record ChangelogMap(Map<String, String> changelogs) {
    public Collection<String> allChangelogs() {
        return changelogs.values();
    }

    public Set<String> versions() {
        return changelogs.keySet();
    }

    public String getVersionChangelog(String version) {
        return changelogs.get(version);
    }

    public boolean isEmpty() {
        return changelogs.isEmpty();
    }
}
