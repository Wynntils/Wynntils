package com.wynntils.mc.event;

import com.wynntils.WynntilsMod;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.util.List;
import java.util.function.Consumer;

public class ScreenEvents {

    public static void onScreenCreated(Screen screen, List<AbstractWidget> buttons, Consumer<AbstractWidget> addButton) {
        System.out.println("DEBUG: onScreenCreated");
        if (screen instanceof TitleScreen titleScreen) {
            WynntilsMod.postTitleScreenInit(titleScreen, buttons, addButton);
        } else if (screen instanceof PauseScreen gameMenuScreen) {
            WynntilsMod.postGameMenuScreenInit(gameMenuScreen, buttons);
        }
    }
}
