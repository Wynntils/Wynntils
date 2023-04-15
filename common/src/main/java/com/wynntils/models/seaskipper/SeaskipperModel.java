/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.seaskipper;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.items.items.gui.SeaskipperDestinationItem;
import com.wynntils.models.map.pois.SeaskipperPoi;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class SeaskipperModel extends Model {
    private static final StyledText OAK_BOAT_NAME = StyledText.fromString("§bOak Boat");
    private static final Pattern SEASKIPPER_PASS_PATTERN = Pattern.compile("^§b(.*) Pass §7for §b(\\d+)²$");

    private static AbstractContainerScreen actualSeaskipperScreen;

    private final Map<SeaskipperDestinationItem, Integer> destinations = new HashMap<>();
    private final List<SeaskipperPoi> seaskipperPois = new ArrayList<>();
    private final List<String> availableNames = new ArrayList<>();

    private List<SeaskipperPoi> availableDestinations = new ArrayList<>();
    private SeaskipperPoi currentPoi;
    private int boatSlot;
    private int containerId = -2;

    public SeaskipperModel(ContainerModel containerModel) {
        super(List.of(containerModel));
    }

    @SubscribeEvent
    public void onScreenOpened(ScreenOpenedEvent.Post event) {
        if (Models.Container.isSeaskipper(
                ComponentUtils.getUnformatted(event.getScreen().getTitle()))) {
            actualSeaskipperScreen = (AbstractContainerScreen) event.getScreen();

            loadSeaskipperPois();
        }
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (Models.Container.isSeaskipper(ComponentUtils.getUnformatted(event.getTitle()))) {
            containerId = event.getContainerId();
        }
    }

    public void closeScreen() {
        McUtils.player().closeContainer();
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent event) {
        if (event.getContainerId() != containerId) return;

        if (StyledText.fromComponent(event.getItemStack().getHoverName()).equals(OAK_BOAT_NAME)) {
            setBoatSlot(event.getSlot());
            return;
        }

        Matcher matcher =
                StyledText.fromComponent(event.getItemStack().getHoverName()).getMatcher(SEASKIPPER_PASS_PATTERN);
        if (!matcher.matches()) return;

        String destination = matcher.group(1);
        int price = Integer.parseInt(matcher.group(2));
        String shorthand = destination.substring(0, 2);

        addDestination(new SeaskipperDestinationItem(destination, price, shorthand), event.getSlot());

        availableNames.add(destination);
    }

    private void addDestination(SeaskipperDestinationItem destination, int slot) {
        destinations.put(destination, slot);
    }

    public Map<SeaskipperDestinationItem, Integer> getDestinations() {
        return Collections.unmodifiableMap(destinations);
    }

    public List<SeaskipperPoi> getAvailableDestinations() {
        return Collections.unmodifiableList(availableDestinations);
    }

    public List<SeaskipperPoi> getSeaskipperPois() {
        return Collections.unmodifiableList(seaskipperPois);
    }

    private void setBoatSlot(int boatSlot) {
        this.boatSlot = boatSlot;
    }

    public void purchaseBoat() {
        clickSlot(boatSlot);
    }

    public SeaskipperPoi getCurrentPoi() {
        return currentPoi;
    }

    public void purchasePass(String destinationToTravelTo) {
        int passSlot = -1;

        for (Map.Entry<SeaskipperDestinationItem, Integer> entry :
                Models.Seaskipper.getDestinations().entrySet()) {
            String destination = entry.getKey().getDestination();

            if (destination.equals(destinationToTravelTo)) {
                passSlot = entry.getValue();
                break;
            }
        }

        if (passSlot == -1) {
            return;
        }

        clickSlot(passSlot);
    }

    private void clickSlot(int slot) {
        ContainerUtils.clickOnSlot(
                slot,
                actualSeaskipperScreen.getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                actualSeaskipperScreen.getMenu().getItems());
    }

    private void loadSeaskipperPois() {
        seaskipperPois.clear();
        availableNames.clear();
        availableDestinations.clear();
        destinations.clear();

        Download dl = Managers.Net.download(UrlId.DATA_STATIC_SEASKIPPER_LOCATIONS);
        dl.handleReader(reader -> {
            Type type = new TypeToken<ArrayList<SeaskipperProfile>>() {}.getType();
            List<SeaskipperProfile> seaskipperProfiles = WynntilsMod.GSON.fromJson(reader, type);

            for (SeaskipperProfile profile : seaskipperProfiles) {
                seaskipperPois.add(new SeaskipperPoi(
                        profile.destination,
                        profile.combatLevel,
                        profile.startX,
                        profile.startZ,
                        profile.endX,
                        profile.endZ));
            }

            for (SeaskipperPoi poi : seaskipperPois) {
                if (poi.isPlayerInside()) {
                    currentPoi = poi;
                    availableDestinations.add(currentPoi);
                    break;
                }
            }

            setAvailableDestinations();
        });
    }

    private void setAvailableDestinations() {
        for (SeaskipperPoi poi : seaskipperPois) {
            if (availableNames.contains(poi.getName())) {
                availableDestinations.add(poi);
            }
        }
    }

    private static class SeaskipperProfile {
        String destination;
        int combatLevel;
        int startX;
        int startZ;
        int endX;
        int endZ;
    }
}
