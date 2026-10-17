/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.notifications.type.RedirectAction;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.models.abilities.event.AbilityCooldownRefreshedEvent;
import com.wynntils.overlays.AbilityCooldownsOverlay;
import com.wynntils.utils.type.RenderElementType;
import java.util.List;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class AbilityCooldownsOverlayFeature extends Feature {
    @RegisterOverlay(renderType = RenderElementType.ACTION_BAR)
    private final AbilityCooldownsOverlay abilityCooldownsOverlay = new AbilityCooldownsOverlay();

    @Persisted
    private final Config<RedirectAction> redirectRefreshedMessages = new Config<>(RedirectAction.HIDE);

    public AbilityCooldownsOverlayFeature() {
        super(
                new ProfileDefault.Builder()
                        .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.LITE)
                        .build(),
                List.of(ConfigDependency.functionality(Models.Ability.trackAbilityCooldowns)));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAbilityCooldownRefreshed(AbilityCooldownRefreshedEvent event) {
        if (redirectRefreshedMessages.get() == RedirectAction.KEEP) return;

        event.setCancelMessage(true);

        if (redirectRefreshedMessages.get() == RedirectAction.REDIRECT) {
            Managers.Notification.queueMessage(event.getMessage());
        }
    }
}
