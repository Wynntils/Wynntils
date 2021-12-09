package com.wynntils.mc.event;

import com.wynntils.WynntilsMod;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.util.List;

public class ScreenEvents {

    public static void onScreenCreated(Screen screen, List<AbstractWidget> buttons) {
        System.out.println("DEBUG: onScreenCreated");
        if (screen instanceof TitleScreen titleScreen) {
            WynntilsMod.postTitleScreenInit(titleScreen, buttons);
        } else if (screen instanceof PauseScreen gameMenuScreen) {
            WynntilsMod.postGameMenuScreenInit(gameMenuScreen, buttons);
        }
    }
}
