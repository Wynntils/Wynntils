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
import com.wynntils.models.items.ItemModel;
import com.wynntils.models.items.items.gui.SeaskipperDestinationItem;
import com.wynntils.models.seaskipper.type.SeaskipperDestination;
import com.wynntils.models.seaskipper.type.SeaskipperDestinationProfile;
import com.wynntils.screens.maps.SeaskipperDepartureBoardScreen;
import com.wynntils.screens.maps.SeaskipperMapScreen;
import com.wynntils.services.map.pois.SeaskipperDestinationPoi;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class SeaskipperModel extends Model {
    private static final StyledText OAK_BOAT_NAME = StyledText.fromString("§bOak Boat");

    private List<SeaskipperDestination> allDestinations = new ArrayList<>();
    private List<SeaskipperDestination> availableDestinations = new ArrayList<>();

    private int boatSlot;
    private int containerId = -2;

    public SeaskipperModel(ItemModel itemModel) {
        super(List.of(itemModel));

        reloadData();
    }

    @Override
    public void reloadData() {
        loadSeaskipperPois();
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (!Models.Container.isSeaskipper(event.getTitle())) return;

        containerId = event.getContainerId();
        availableDestinations = new ArrayList<>();
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getContainerId() != containerId) return;

        if (StyledText.fromComponent(event.getItemStack().getHoverName()).equals(OAK_BOAT_NAME)) {
            boatSlot = event.getSlot();
            return;
        }

        Optional<SeaskipperDestinationItem> optionalItem =
                Models.Item.asWynnItem(event.getItemStack(), SeaskipperDestinationItem.class);

        if (optionalItem.isEmpty()) return;

        SeaskipperDestinationItem item = optionalItem.get();

        Optional<SeaskipperDestination> destinationOptional = allDestinations.stream()
                .filter(profile -> profile.profile().destination().equals(item.getDestination()))
                .findFirst();

        if (destinationOptional.isEmpty()) {
            WynntilsMod.warn("Could not find profile for destination: " + item.getDestination());
            return;
        }

        SeaskipperDestinationProfile profile = destinationOptional.get().profile();

        availableDestinations.add(new SeaskipperDestination(profile, item, event.getSlot()));

        // We added a new destination, reload the map
        // (This reloads the pois for every item parsed, but performance is not an issue here)
        if (McUtils.mc().screen instanceof SeaskipperMapScreen seaskipperMapScreen) {
            seaskipperMapScreen.reloadDestinationPois();
        } else if (McUtils.mc().screen instanceof SeaskipperDepartureBoardScreen seaskipperDepartureBoardScreen) {
            seaskipperDepartureBoardScreen.reloadDestinationPois();
        }
    }

    public List<SeaskipperDestinationPoi> getPois(boolean includeAll) {
        List<SeaskipperDestinationPoi> pois = new ArrayList<>();

        for (SeaskipperDestination destination : availableDestinations) {
            pois.add(new SeaskipperDestinationPoi(destination));
        }

        // Include the destination we are currently at
        allDestinations.stream()
                .filter(SeaskipperDestination::isPlayerInside)
                .findFirst()
                .ifPresent(profile -> pois.add(new SeaskipperDestinationPoi(profile)));

        if (includeAll) {
            List<SeaskipperDestination> notAvailableProfiles = allDestinations.stream()
                    .filter(profile -> pois.stream()
                            .map(SeaskipperDestinationPoi::getDestination)
                            .noneMatch(destination -> destination.profile().equals(profile.profile())))
                    .toList();

            pois.addAll(notAvailableProfiles.stream()
                    .map(SeaskipperDestinationPoi::new)
                    .toList());
        }

        return pois;
    }

    public void purchaseBoat() {
        ContainerUtils.clickOnSlot(
                boatSlot,
                containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());
    }

    public void purchasePass(SeaskipperDestination destination) {
        if (destination.slot() == -1) return;

        ContainerUtils.clickOnSlot(
                destination.slot(),
                containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());
    }

    public boolean isProfileLoaded() {
        return !allDestinations.isEmpty();
    }

    private void loadSeaskipperPois() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_SEASKIPPER_DESTINATIONS);

        dl.handleReader(reader -> {
            Type type = new TypeToken<ArrayList<SeaskipperDestinationProfile>>() {}.getType();
            List<SeaskipperDestinationProfile> profiles = WynntilsMod.GSON.fromJson(reader, type);

            allDestinations = profiles.stream()
                    .map(profile -> new SeaskipperDestination(profile, null, -1))
                    .toList();
        });
    }
}
