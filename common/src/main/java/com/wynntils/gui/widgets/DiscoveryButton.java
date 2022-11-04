/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.wynntils.wynn.model.discoveries.objects.DiscoveryInfo;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public class DiscoveryButton extends AbstractButton {
    private final DiscoveryInfo discoveryInfo;

    public DiscoveryButton(int x, int y, int width, int height, DiscoveryInfo discoveryInfo) {
        super(x, y, width, height, new TextComponent("Discovery Button"));
        this.discoveryInfo = discoveryInfo;
    }

    @Override
    public void onPress() {}

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
