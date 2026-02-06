/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.commands;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.CommandSuggestionEvent;
import java.util.Set;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMMANDS)
public class FilterAdminCommandsFeature extends Feature {
    private static final Set<String> FILTERED_COMMANDS = Set.of(
            "bungee",
            "change",
            "connect",
            "galert",
            "gcountdown",
            "glist",
            "gsend",
            "lobby",
            "perms",
            "pfind",
            "plist",
            "pwlist",
            "sendtoall",
            "servers",
            "sparkb",
            "sparkbungee",
            "wcl",
            "wynnproxy");

    public FilterAdminCommandsFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }

    @SubscribeEvent
    public void onModifySuggestions(CommandSuggestionEvent.Modify event) {
        FILTERED_COMMANDS.forEach(event::removeSuggestion);
    }
}
