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
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WynncraftPauseScreenFeature extends UserFeature {
    @SubscribeEvent
    public void onPauseScreenInitEvent(PauseMenuInitEvent event) {
        PauseScreen pauseScreen = event.getPauseScreen();

        Optional<Renderable> gridOpt = pauseScreen.renderables.stream()
                .filter(x -> x instanceof GridWidget)
                .findFirst();
        if (gridOpt.isEmpty()) return;

        GridWidget grid = (GridWidget) gridOpt.get();

        List<Button> replacedButtons = new ArrayList<>();

        for (GridWidget.CellInhabitant child : grid.cellInhabitants) {
            if (child.child instanceof Button) {
                replacedButtons.add((Button) child.child);
            }
        }

        Button territoryMap = replaceButtonFunction(
                replacedButtons.get(1),
                Component.translatable("feature.wynntils.wynncraftPauseScreen.territoryMap.name")
                        .withStyle(ChatFormatting.DARK_AQUA),
                (button) -> McUtils.mc().setScreen(GuildMapScreen.create()));
        replacedButtons.set(1, territoryMap);

        Button wynntilsMenu = replaceButtonFunction(
                replacedButtons.get(2),
                Component.translatable("feature.wynntils.wynncraftPauseScreen.wynntilsMenuButton.name"),
                (button) -> McUtils.mc().setScreen(WynntilsMenuScreen.create()));
        replacedButtons.set(2, wynntilsMenu);

        Button classSelection = replaceButtonFunction(
                replacedButtons.get(3),
                Component.translatable("feature.wynntils.wynncraftPauseScreen.classSelectionButton.name"),
                (button) -> {
                    McUtils.mc().setScreen(null);
                    McUtils.mc().mouseHandler.grabMouse();
                    McUtils.sendCommand("class");
                });

        replacedButtons.set(3, classSelection);

        Button hub = replaceButtonFunction(
                replacedButtons.get(4),
                Component.translatable("feature.wynntils.wynncraftPauseScreen.hubButton.name"),
                (button) -> {
                    McUtils.mc().setScreen(null);
                    McUtils.mc().mouseHandler.grabMouse();
                    McUtils.sendCommand("hub");
                });

        replacedButtons.set(4, hub);

        GridWidget newGridWidget = new GridWidget();

        // Inject back non-buttons to grid
        for (GridWidget.CellInhabitant child : grid.cellInhabitants) {
            if (!(child.child instanceof Button)) {
                newGridWidget.addChild(child.child, child.row, child.column);
            }
        }

        // Inject back buttons to grid
        int buttonIndex = 0;
        List<GridWidget.CellInhabitant> cellInhabitants = grid.cellInhabitants;
        for (GridWidget.CellInhabitant child : cellInhabitants) {
            if (child.child instanceof Button) {
                newGridWidget.addChild(replacedButtons.get(buttonIndex), child.row, child.column);
                buttonIndex++;
            }
        }

        // Remove old grid, add back new
        pauseScreen.removeWidget(grid);
        pauseScreen.addRenderableWidget(newGridWidget);
    }

    private Button replaceButtonFunction(Button widget, Component component, Button.OnPress onPress) {
        return new Button.Builder(component, onPress)
                .pos(widget.getX(), widget.getY())
                .size(widget.getWidth(), widget.getHeight())
                .build();
    }
}
