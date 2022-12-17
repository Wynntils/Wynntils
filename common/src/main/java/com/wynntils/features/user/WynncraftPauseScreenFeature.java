/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.gui.screens.WynntilsMenuScreen;
import com.wynntils.gui.screens.maps.GuildMapScreen;
import com.wynntils.mc.event.PauseMenuInitEvent;
import com.wynntils.mc.utils.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WynncraftPauseScreenFeature extends UserFeature {
    @SubscribeEvent
    public void onPauseScreenInitEvent(PauseMenuInitEvent event) {
        PauseScreen pauseScreen = event.getPauseScreen();
        List<Widget> renderables = new ArrayList<>(pauseScreen.renderables);

        Button territoryMap = replaceButtonFunction(
                (Button) renderables.get(1),
                new TranslatableComponent("feature.wynntils.wynncraftPauseScreen.territoryMap.name")
                        .withStyle(ChatFormatting.DARK_AQUA),
                (button) -> McUtils.mc().setScreen(GuildMapScreen.create()));
        renderables.set(1, territoryMap);

        Button wynntilsMenu = replaceButtonFunction(
                (Button) renderables.get(2),
                new TranslatableComponent("feature.wynntils.wynncraftPauseScreen.wynntilsMenuButton.name"),
                (button) -> McUtils.mc().setScreen(WynntilsMenuScreen.create()));
        renderables.set(2, wynntilsMenu);

        Button classSelection = replaceButtonFunction(
                (Button) renderables.get(3),
                new TranslatableComponent("feature.wynntils.wynncraftPauseScreen.classSelectionButton.name"),
                (button) -> {
                    McUtils.mc().setScreen(null);
                    McUtils.mc().mouseHandler.grabMouse();
                    McUtils.player().chat("/class");
                });

        renderables.set(3, classSelection);

        Button hub = replaceButtonFunction(
                (Button) renderables.get(4),
                new TranslatableComponent("feature.wynntils.wynncraftPauseScreen.hubButton.name"),
                (button) -> {
                    McUtils.mc().setScreen(null);
                    McUtils.mc().mouseHandler.grabMouse();
                    McUtils.player().chat("/hub");
                });

        renderables.set(4, hub);

        event.getPauseScreen().clearWidgets();

        for (Widget renderable : renderables) {
            event.getAddButton().accept((AbstractWidget) renderable);
        }
    }

    private Button replaceButtonFunction(Button widget, Component translatableComponent, Button.OnPress onPress) {
        return new Button(widget.x, widget.y, widget.getWidth(), widget.getHeight(), translatableComponent, onPress);
    }
}
