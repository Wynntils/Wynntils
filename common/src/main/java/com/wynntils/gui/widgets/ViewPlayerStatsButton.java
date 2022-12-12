/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.Utils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class ViewPlayerStatsButton extends AbstractButton {
    private static final String STATS_URL_BASE = "https://wynncraft.com/stats/player/";

    private final String playerName;

    public ViewPlayerStatsButton(int x, int y, int width, int height, String playerName) {
        super(x, y, width, height, Component.literal("↵"));
        this.playerName = playerName;
    }

    @Override
    public void onPress() {
        McUtils.playSound(SoundEvents.UI_BUTTON_CLICK.value());

        Utils.openUrl(STATS_URL_BASE + playerName);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
