/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.TickAlwaysEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.User;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class AccountSwitchDetectionFeature extends Feature {
    private User currentAccount = null;

    @SubscribeEvent
    public void onTickAlways(TickAlwaysEvent e) {
        if (Models.WorldState.getCurrentState() != WorldState.NOT_CONNECTED)
            return; // account switches only happen when not connected

        if (currentAccount == null) {
            currentAccount = McUtils.mc().getUser();
        } else if (currentAccount.getProfileId() != McUtils.mc().getUser().getProfileId()) {
            currentAccount = McUtils.mc().getUser();
            WynntilsMod.info("Account switch detected, reloading configs");
            Managers.Config.userSwitched();
        }
    }
}
