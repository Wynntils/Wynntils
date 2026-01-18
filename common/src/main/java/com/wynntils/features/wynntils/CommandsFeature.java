/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.CommandSentEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class CommandsFeature extends Feature {
    public CommandsFeature() {
        // We don't use ProfileDefault.ENABLED for this as it is one method of accessing the Wynntils menu that
        // has minimal impact on gameplay so we want it enabled for BLANK_SLATE
        super(new ProfileDefault.Builder()
                .enabledFor(
                        ConfigProfile.DEFAULT,
                        ConfigProfile.NEW_PLAYER,
                        ConfigProfile.LITE,
                        ConfigProfile.MINIMAL,
                        ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCommand(CommandSentEvent e) {
        String command = e.getCommand();

        if (Managers.Command.handleCommand(command)) {
            e.setCanceled(true);
        }
    }
}
