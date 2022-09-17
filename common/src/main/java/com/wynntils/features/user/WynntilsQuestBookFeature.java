/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.WynntilsMenuScreen;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WynntilsQuestBookFeature extends UserFeature {
    private static final Pattern QUEST_BOOK_PATTERN = Pattern.compile("^§0\\[Pg. \\d+\\] §8.*§0 Quests$");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (McUtils.player().isShiftKeyDown()) return;

        String menuTitle = ComponentUtils.getCoded(event.getTitle());

        if (!QUEST_BOOK_PATTERN.matcher(menuTitle).matches()) {
            return;
        }

        event.setCanceled(true);
        McUtils.mc().setScreen(new WynntilsMenuScreen());
    }
}
