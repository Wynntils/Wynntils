/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import net.minecraft.network.chat.ComponentSerialization;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.DEBUG)
public class PlayerInfoFooterDebuggerFeature extends Feature {
    private static final String LOG_PREFIX = "[PlayerInfoFooterChangedEvent]";

    public PlayerInfoFooterDebuggerFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent
    public void onPlayerInfoFooterChanged(PlayerInfoFooterChangedEvent event) {
        String footerString = event.getFooter().getString();
        if (footerString.isEmpty()) return;

        WynntilsMod.info(LOG_PREFIX + " string=" + footerString);

        JsonElement footerJson = ComponentSerialization.CODEC
                .encodeStart(JsonOps.INSTANCE, event.getFooter().getComponent())
                .result()
                .orElse(null);

        if (footerJson == null) {
            WynntilsMod.warn(LOG_PREFIX + " Failed to serialize footer to JSON");
            return;
        }

        WynntilsMod.info(LOG_PREFIX + " json=" + WynntilsMod.GSON.toJson(footerJson));
    }
}
