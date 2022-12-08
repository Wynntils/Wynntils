/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.wynntils.core.net.Reference;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.Utils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;

public class ViewPlayerStatsButton extends AbstractButton {
    private final String playerName;

    public ViewPlayerStatsButton(int x, int y, int width, int height, String playerName) {
        super(x, y, width, height, new TextComponent("↵"));
        this.playerName = playerName;
    }

    @Override
    public void onPress() {
        McUtils.playSound(SoundEvents.UI_BUTTON_CLICK);
        Utils.openUrl(Reference.URLs.createPlayerStats(playerName));
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
