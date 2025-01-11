package com.wynntils.screens.gearviewer.widgets;

import net.minecraft.network.chat.Component;

/**
 * A simple button in the gear viewer that executes a single runnable.
 */
public class SimplePlayerInteractionButton extends PlayerInteractionButton {
    private final Runnable runnable;

    public SimplePlayerInteractionButton(int x, int y, Component hoverText, Component buttonText, Runnable runnable) {
        super(x, y, hoverText, buttonText);
        this.runnable = runnable;
    }

    @Override
    public void onPress() {
        super.onPress();
        runnable.run();
    }
}
