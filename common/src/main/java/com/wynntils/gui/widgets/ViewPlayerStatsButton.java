/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.wynntils.core.components.Managers;
import com.wynntils.core.net.UrlId;
import com.wynntils.mc.utils.McUtils;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class ViewPlayerStatsButton extends WynntilsButton {
    private final String playerName;

    public ViewPlayerStatsButton(int x, int y, int width, int height, String playerName) {
        super(x, y, width, height, Component.literal("↵"));
        this.playerName = playerName;
    }

    @Override
    public void onPress() {
        McUtils.playSound(SoundEvents.UI_BUTTON_CLICK.value());
        Managers.Net.openLink(UrlId.LINK_WYNNCRAFT_PLAYER_STATS, Map.of("username", playerName));
    }
}
