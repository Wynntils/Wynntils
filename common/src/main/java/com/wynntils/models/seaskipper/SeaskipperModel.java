/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.seaskipper;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.mod.event.WynntilsInitEvent;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.containers.containers.SeaskipperContainer;
import com.wynntils.models.items.items.gui.SeaskipperDestinationItem;
import com.wynntils.models.seaskipper.providers.SeaskipperDestinationAreaProvider;
import com.wynntils.models.seaskipper.type.SeaskipperDestination;
import com.wynntils.models.seaskipper.type.SeaskipperDestinationProfile;
import com.wynntils.screens.maps.CustomSeaskipperScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class SeaskipperModel extends Model {
    private static final SeaskipperDestinationAreaProvider SEASKIPPER_DESTINATION_AREA_PROVIDER =
            new SeaskipperDestinationAreaProvider();
    private static final StyledText OAK_BOAT_NAME = StyledText.fromString("§bOak Boat");

    private final List<SeaskipperDestination> allDestinations = new CopyOnWriteArrayList<>();
    private List<SeaskipperDestination> availableDestinations = new ArrayList<>();

    private int boatSlot = -1;
    private int containerId = -2;

    public SeaskipperModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onModInitFinished(WynntilsInitEvent.ModInitFinished event) {
        Services.MapData.registerBuiltInProvider(SEASKIPPER_DESTINATION_AREA_PROVIDER);
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_SEASKIPPER_DESTINATIONS)
                .handleReader(this::handleSeaskipperDestinations);
    }

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent.Pre e) {
        if (!(Models.Container.getCurrentContainer() instanceof SeaskipperContainer seaskipperContainer)) return;

        containerId = seaskipperContainer.getContainerId();
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Post event) {
        if (event.getContainerId() != containerId) return;
        List<SeaskipperDestination> newAvailableDestinations = new ArrayList<>();

        for (int i = 0; i < event.getItems().size(); i++) {
            ItemStack item = event.getItems().get(i);

            if (boatSlot == -1 && StyledText.fromComponent(item.getHoverName()).equals(OAK_BOAT_NAME)) {
                boatSlot = i;
                continue;
            }

            Optional<SeaskipperDestinationItem> optionalItem =
                    Models.Item.asWynnItem(item, SeaskipperDestinationItem.class);

            if (optionalItem.isEmpty()) continue;

            SeaskipperDestinationItem destinationItem = optionalItem.get();

            Optional<SeaskipperDestination> destinationOptional = allDestinations.stream()
                    .filter(profile -> profile.profile().destination().equals(destinationItem.getDestination()))
                    .findFirst();

            if (destinationOptional.isEmpty()) {
                WynntilsMod.warn("Could not find profile for destination: " + destinationItem.getDestination());
                continue;
            }

            SeaskipperDestinationProfile profile = destinationOptional.get().profile();

            SeaskipperDestination newDestination = new SeaskipperDestination(profile, destinationItem, i);
            newAvailableDestinations.add(newDestination);
        }

        availableDestinations = newAvailableDestinations;
        allDestinations.removeIf(destination -> availableDestinations.stream()
                .anyMatch(available -> available.profile().equals(destination.profile())));
        allDestinations.addAll(availableDestinations);

        SEASKIPPER_DESTINATION_AREA_PROVIDER.updateDestinations(allDestinations);

        // Reload the map
        if (McUtils.mc().screen instanceof CustomSeaskipperScreen customSeaskipperScreen) {
            customSeaskipperScreen.reloadDestinations();
        }
    }

    public List<SeaskipperDestination> getDestinations(boolean includeAll) {
        List<SeaskipperDestination> destinations = new ArrayList<>(availableDestinations);

        // Include the destination we are currently at
        allDestinations.stream()
                .filter(SeaskipperDestination::isPlayerInside)
                .findFirst()
                .ifPresent(destinations::add);

        if (includeAll) {
            allDestinations.stream()
                    .filter(profile -> destinations.stream()
                            .noneMatch(destination -> destination.profile().equals(profile.profile())))
                    .forEach(destinations::add);
        }

        return destinations;
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

    private void handleSeaskipperDestinations(Reader reader) {
        Type type = new TypeToken<ArrayList<SeaskipperDestinationProfile>>() {}.getType();
        List<SeaskipperDestinationProfile> profiles = WynntilsMod.GSON.fromJson(reader, type);

        allDestinations.clear();
        allDestinations.addAll(profiles.stream()
                .map(profile -> new SeaskipperDestination(profile, null, -1))
                .toList());
        SEASKIPPER_DESTINATION_AREA_PROVIDER.updateDestinations(allDestinations);
    }
}
