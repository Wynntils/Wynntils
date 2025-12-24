/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class WynntilsHintMessagesFeature extends Feature {
    @Persisted
    private final Config<Boolean> firstJoinOnly = new Config<>(true);

    public WynntilsHintMessagesFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        if (e.isFirstJoinWorld() || e.getNewState() == WorldState.WORLD && !firstJoinOnly.get()) {
            Services.Hint.sendHint();
        }
    }
}
