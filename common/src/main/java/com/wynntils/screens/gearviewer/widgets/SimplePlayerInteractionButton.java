package com.wynntils.screens.gearviewer.widgets;

import com.wynntils.utils.render.Texture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
