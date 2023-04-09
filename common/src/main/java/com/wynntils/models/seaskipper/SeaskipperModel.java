/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.seaskipper;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.items.items.gui.SeaskipperDestinationItem;
import com.wynntils.screens.maps.SeaskipperMapScreen;
import com.wynntils.utils.mc.ComponentUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SeaskipperModel extends Model {
    private static final Pattern SEASKIPPER_PASS_PATTERN = Pattern.compile("^§b(.*) Pass §7for §b(\\d+)²$");
    private int containerId = -2;
    private static SeaskipperMapScreen currentScreen;

    public SeaskipperModel(ContainerModel containerModel) {
        super(List.of(containerModel));
    }

    @SubscribeEvent
    public void onScreenOpened(ScreenOpenedEvent.Post event) {
        if (event.getScreen() instanceof SeaskipperMapScreen seaskipperMapScreen) {
            currentScreen = seaskipperMapScreen;
        }
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (Models.Container.isSeaskipper(ComponentUtils.getUnformatted(event.getTitle()))) {
            containerId = event.getContainerId();
        }
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent event) {
        if (currentScreen == null) return;
        if (event.getContainerId() != containerId) return;
        currentScreen.addItemStack(event.getItemStack());

        if (event.getItemStack().getHoverName().getString().equals("§bOak Boat")) {
            currentScreen.setBoatSlot(event.getSlot());
        }

        Matcher matcher = SEASKIPPER_PASS_PATTERN.matcher(event.getItemStack().getHoverName().getString());
        if (!matcher.matches()) return;

        String destination = matcher.group(1);
        int price = Integer.parseInt(matcher.group(2));
        String shorthand = destination.substring(0, 2);

        currentScreen.addDestination(new SeaskipperDestinationItem(destination, price, shorthand), event.getSlot());
    }
}