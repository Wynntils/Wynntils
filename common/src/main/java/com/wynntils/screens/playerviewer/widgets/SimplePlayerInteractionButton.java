/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.playerviewer.widgets;

import com.wynntils.utils.render.Texture;
import net.minecraft.network.chat.Component;

/**
 * A simple button in the gear viewer that executes a single runnable.
 */
public class SimplePlayerInteractionButton extends PlayerInteractionButton {
    private final Runnable runnable;

    public SimplePlayerInteractionButton(int x, int y, Component hoverText, Texture icon, Runnable runnable) {
        super(x, y, hoverText, icon);
        this.runnable = runnable;
    }

    @Override
    public void onPress() {
        super.onPress();
        runnable.run();
    }
}
