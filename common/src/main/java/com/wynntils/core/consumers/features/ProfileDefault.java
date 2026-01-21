/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

import com.wynntils.core.persisted.config.ConfigProfile;
import java.util.EnumMap;
import java.util.Map;

public record ProfileDefault(Map<ConfigProfile, Boolean> defaults) {
    public static final ProfileDefault ENABLED = new Builder().setAll(true).build();
    public static final ProfileDefault DISABLED = new Builder().setAll(false).build();

    public boolean getDefault(ConfigProfile profile) {
        return defaults.getOrDefault(profile, false);
    }

    public static ProfileDefault onlyDefault() {
        ProfileDefault.Builder builder = new ProfileDefault.Builder();

        builder.enabledFor(ConfigProfile.DEFAULT);

        return builder.build();
    }

    public static class Builder {
        private final Map<ConfigProfile, Boolean> defaults = new EnumMap<>(ConfigProfile.class);

        private Builder setAll(boolean value) {
            for (ConfigProfile profile : ConfigProfile.values()) {
                // Blank slate is always disabled unless explicitly enabled in enabledFor
                if (profile == ConfigProfile.BLANK_SLATE) continue;

                defaults.put(profile, value);
            }
            return this;
        }

        public Builder enabledFor(ConfigProfile... profiles) {
            for (ConfigProfile profile : profiles) {
                defaults.put(profile, true);
            }

            return this;
        }

        public ProfileDefault build() {
            return new ProfileDefault(defaults);
        }
    }
}
