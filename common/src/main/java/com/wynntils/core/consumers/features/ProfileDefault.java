/*
 * Copyright © Wynntils 2025.
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
        return defaults.getOrDefault(profile, true);
    }

    public static class Builder {
        private final Map<ConfigProfile, Boolean> defaults = new EnumMap<>(ConfigProfile.class);

        private Builder setAll(boolean value) {
            for (ConfigProfile profile : ConfigProfile.values()) {
                defaults.put(profile, value);
            }
            return this;
        }

        public Builder disableFor(ConfigProfile... profiles) {
            for (ConfigProfile profile : profiles) {
                defaults.put(profile, false);
            }

            return this;
        }

        public ProfileDefault build() {
            return new ProfileDefault(defaults);
        }
    }
}
