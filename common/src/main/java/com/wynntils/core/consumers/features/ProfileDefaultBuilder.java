/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

import com.wynntils.core.persisted.config.ConfigProfile;
import java.util.EnumMap;
import java.util.Map;

public final class ProfileDefaultBuilder {
    private final Map<ConfigProfile, Boolean> defaults = new EnumMap<>(ConfigProfile.class);

    private ProfileDefaultBuilder() {}

    public static ProfileDefaultBuilder enabledForAll() {
        ProfileDefaultBuilder builder = new ProfileDefaultBuilder();
        for (ConfigProfile profile : ConfigProfile.values()) {
            builder.defaults.put(profile, true);
        }

        return builder;
    }

    public static ProfileDefaultBuilder disabledForAll() {
        ProfileDefaultBuilder builder = new ProfileDefaultBuilder();
        for (ConfigProfile profile : ConfigProfile.values()) {
            builder.defaults.put(profile, false);
        }

        return builder;
    }

    public static ProfileDefaultBuilder disabledForProfiles(ConfigProfile... profiles) {
        ProfileDefaultBuilder builder = enabledForAll();
        for (ConfigProfile profile : profiles) {
            builder.defaults.put(profile, false);
        }

        return builder;
    }

    public boolean getDefault(ConfigProfile profile) {
        return defaults.getOrDefault(profile, true);
    }
}
