/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.PauseMenuInitEvent;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.maps.GuildMapScreen;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class WynncraftPauseScreenFeature extends Feature {
    @SubscribeEvent
    public void onPauseScreenInitEvent(PauseMenuInitEvent event) {
        PauseScreen pauseScreen = event.getPauseScreen();
        List<Renderable> renderables = new ArrayList<>(pauseScreen.renderables);

        Button territoryMap = replaceButtonFunction(
                (Button) renderables.get(1),
                Component.translatable("feature.wynntils.wynncraftPauseScreen.territoryMap.name")
                        .withStyle(ChatFormatting.DARK_AQUA),
                (button) -> McUtils.mc().setScreen(GuildMapScreen.create()));
        renderables.set(1, territoryMap);

        Button wynntilsMenu = replaceButtonFunction(
                (Button) renderables.get(2),
                Component.translatable("feature.wynntils.wynncraftPauseScreen.wynntilsMenuButton.name"),
                (button) -> WynntilsMenuScreenBase.openBook(WynntilsMenuScreen.create()));
        renderables.set(2, wynntilsMenu);

        Button classSelection = replaceButtonFunction(
                (Button) renderables.get(3),
                Component.translatable("feature.wynntils.wynncraftPauseScreen.classSelectionButton.name"),
                (button) -> {
                    McUtils.mc().setScreen(null);
                    McUtils.mc().mouseHandler.grabMouse();
                    McUtils.sendCommand("class");
                });

        renderables.set(3, classSelection);

        // If the class is not a button, it is overriden by another mod (ModMenu)
        if (renderables.get(4).getClass() == Button.class) {
            Button hub = replaceButtonFunction(
                    (Button) renderables.get(4),
                    Component.translatable("feature.wynntils.wynncraftPauseScreen.hubButton.name"),
                    (button) -> {
                        McUtils.mc().setScreen(null);
                        McUtils.mc().mouseHandler.grabMouse();
                        McUtils.sendCommand("hub");
                    });

            renderables.set(4, hub);
        }

        event.getPauseScreen().clearWidgets();

        for (Renderable renderable : renderables) {
            if (renderable instanceof AbstractWidget widget) {
                event.getPauseScreen().addRenderableWidget(widget);
            }
        }
    }

    private Button replaceButtonFunction(Button widget, Component component, Button.OnPress onPress) {
        return new Button.Builder(component, onPress)
                .pos(widget.getX(), widget.getY())
                .size(widget.getWidth(), widget.getHeight())
                .build();
    }
}
