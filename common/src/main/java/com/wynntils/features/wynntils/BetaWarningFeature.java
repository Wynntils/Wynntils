/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.PlayerInfoEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class BetaWarningFeature extends Feature {
    private boolean warn = false;

    @SubscribeEvent
    public void onConnect(WynncraftConnectionEvent.Connected event) {
        if (event.getHost().equals("beta") != WynntilsMod.isPreAlpha()) {
            warn = true;
        }
    }

    // We use this event because WorldStateModel might break on beta
    @SubscribeEvent
    public void onDisplayNameChange(PlayerInfoEvent.PlayerDisplayNameChangeEvent e) {
        if (!warn) return;

        warn = false;
        McUtils.sendMessageToClient(
                Component.literal(
                        "You are using a pre-alpha version of Wynntils. This version is only supported on Wynncraft Beta. Please use the normal alpha version, or connect to the beta server instead."));
    }
}
