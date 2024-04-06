/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.CommandSentEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class CommandsFeature extends Feature {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCommand(CommandSentEvent e) {
        String command = e.getCommand();

        if (Managers.Command.handleCommand(command)) {
            e.setCanceled(true);
        }
    }
}
